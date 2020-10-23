package net.accela.telnet.server;

import net.accela.prisma.PrismaWM;
import net.accela.prisma.util.CharsetDecoder;
import net.accela.telnet.exception.InvalidTelnetSequenceException;
import net.accela.telnet.exception.TerminationException;
import net.accela.telnet.session.InputParser;
import net.accela.telnet.session.TelnetSession;
import net.accela.telnet.util.ArrayUtil;
import net.accela.telnet.util.TelnetByteTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static net.accela.telnet.util.TelnetBytes.*;

public final class TelnetSessionServer extends Thread {
    // The session this TelnetSessionServer is serving
    final TelnetSession session;
    final InputStream inputStream;
    final OutputStream outputStream;
    public @Nullable InputStream fromWindowManager;
    public @Nullable OutputStream toWindowManager;

    // FIXME: 10/21/20 If there are better ways of doing this, please let me know.
    public void setWindowManagerStreams(@Nullable InputStream fromWindowManager,
                                        @Nullable OutputStream toWindowManager) {
        // Set values
        this.fromWindowManager = fromWindowManager;
        this.toWindowManager = toWindowManager;

        // Start a new reader thread or interrupt the old one if the new stream is null
        if (fromWindowManager == null) {
            if (fromWMReaderThread != null) fromWMReaderThread.interrupt();
        } else newWMReaderThread();
    }

    @Nullable Thread fromWMReaderThread;

    public void newWMReaderThread() {
        // Interrupt first if needed
        if (fromWMReaderThread != null && !fromWMReaderThread.isInterrupted()) fromWMReaderThread.interrupt();

        // Create the new thread
        fromWMReaderThread = new Thread(() -> {
            Thread.currentThread().setName(TelnetSession.class.getSimpleName()
                    + ":" + session.getUUID() + ":fromEngineReader");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    int read = fromWindowManager.read();
                    if (read == -1) throw new IOException("End of stream");
                    // Note that this is not just just forcibly sent, writeToTerminal uses a lock mechanism.
                    // It cannot interrupt ongoing negotiation as far as I'm aware.
                    writeToClient((byte) read);
                }
            } catch (IOException ex) {
                session.getLogger().log(Level.WARNING, "Exception in fromWMReaderThread", ex);
            }
        });
        // Start the new thread
        fromWMReaderThread.start();
    }

    // Charset configuration
    public final static Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    public final static Charset ASCII_CHARSET = StandardCharsets.US_ASCII;
    public final static Charset IBM437_CHARSET = Charset.forName("IBM437");
    public final List<Charset> supportedCharsets = new ArrayList<>() {{
        add(UTF8_CHARSET);
        add(ASCII_CHARSET);
        add(IBM437_CHARSET);
    }};
    @NotNull Charset currentCharset = UTF8_CHARSET;
    // CharsetDecoder
    CharsetDecoder charsetDecoder = new CharsetDecoder() {
        @Override
        protected @NotNull Charset getCurrentCharset() {
            return currentCharset;
        }
    };

    /**
     * The current state of negotiation
     */
    enum NegotiationState {
        WAITING_FOR_IAC_OR_CHR,    // When waiting for IAC or a character
        DECODING_CHR,              // When currently decoding a character (ignores IAC)
        WAITING_FOR_COMMAND,       // When waiting for a telnet sequence command
        WAITING_FOR_ARGUMENTS,     // When waiting for a telnet sequence argument
        SUBNEGOTIATION_INTERRUPTED // When subnegotiation was interrupted
    }

    @NotNull NegotiationState negotiationState = NegotiationState.WAITING_FOR_IAC_OR_CHR;

    // Synchronization lock
    final Object terminalWriteLock = new Object();

    // Default negotiation result flags
    boolean echoEnabled = true;
    boolean suppressGoAheadEnabled = false;
    boolean transmitBinaryEnabled = false;

    // The Key is the received byte trigger (not a full sequence), and the Value is a list of responses
    final HashMap<Byte, List<TelnetResponse>> negotiationFlow = new HashMap<>();

    // Sequences pending to be sent
    // When adding a sequence to be sent, you may also supply it with a few response sequences,
    // that get registered in 'negotiations'. A response may change a TelnetOption in the TelnetSession.
    final List<TelnetSequence> pendingSequences = new ArrayList<>();

    // A temporary cache of the sequence being received from the client
    TelnetSequence tmpReceivedSequence = new TelnetSequence();
    // A temporary cache of the sequence arguments being received from the client
    List<Byte> tmpReceivedArguments = new ArrayList<>();

    // Responsible for relaying input to the Engine
    final InputParser inputParser;

    public TelnetSessionServer(@NotNull TelnetSession session, @NotNull Socket socket) throws IOException {
        this.session = session;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.inputParser = new InputParser(session, this);

        // Register default negotiation for logout
        registerNegotiationFlow(LOGOUT, fullTrigger -> {
            sendSequenceWhenNotNegotiating(new TelnetSequence(WILL, LOGOUT));
            session.close("Logout requested by the client");
        });
    }

    @Override
    public void run() {
        Thread.currentThread().setName(TelnetSessionServer.class.getSimpleName() + ":" + session.getUUID());

        String terminationReason = null;
        try {
            // Start negotiating basic functionality
            negotiateEcho(false);
            negotiateSuppressGoAhead(true);
            negotiateTransmitBinary(true);
            negotiateCharset(supportedCharsets.toArray(new Charset[0]));

            // Start reader thread
            while (!isInterrupted()) {
                // Send pending sequences, if any
                sendPendingSequence();

                // Proceed to parse
                parse();
            }
        } catch (IOException ex) {
            // If it was interrupted then this exception is safe to ignore
            // However, if it wasn't interrupted then it's actually a bug and we need to log this.
            if (!isInterrupted()) {
                session.getLogger().log(Level.WARNING, "IOException in TelnetSessionServer", ex);
            }
        } catch (TerminationException ex) {
            // If intentionally terminated, we need to know why
            terminationReason = ex.getMessage();
        }

        // Close the Session if there's a reason for it, otherwise just stop this thread by letting it complete.
        if (terminationReason != null) session.close(terminationReason);
    }

    //
    // --- PARSING ---
    //
    void parse() throws IOException, TerminationException {
        byte by = readFromTerminal();

        switch (negotiationState) {
            case WAITING_FOR_IAC_OR_CHR:
                // If it's the start of a telnet sequence
                if (by == IAC) {
                    negotiationState = NegotiationState.WAITING_FOR_COMMAND;
                    break;
                }
                // Else it's a character
                else {
                    negotiationState = NegotiationState.DECODING_CHR;
                }
                // Continue down, don't break
            case DECODING_CHR:
                while (true) {
                    // Attempt to decode the byte we got
                    String decodedString = charsetDecoder.decodeByte(by);

                    // If we did not decode a character yet
                    if (decodedString == null) {
                        // Read a new byte and let it loop until it returns a character
                        by = readFromTerminal();
                    }
                    // If we decoded a character
                    else {
                        // Reset state
                        negotiationState = NegotiationState.WAITING_FOR_IAC_OR_CHR;
                        // Send to engine
                        writeToEngine(decodedString);
                        // Break the loop
                        break;
                    }
                }
                break;
            case WAITING_FOR_COMMAND:
                tmpReceivedSequence.setCommandByte(by);
                switch (by) {
                    case SB:
                    case WILL:
                    case WONT:
                    case DO:
                    case DONT:
                        negotiationState = NegotiationState.WAITING_FOR_ARGUMENTS;
                        break;
                    case IP:
                        parseCompleteSequence();
                        negotiationState = NegotiationState.WAITING_FOR_IAC_OR_CHR;
                        break;
                    case IAC:
                        break;
                    default:
                        // Did not receive a valid command, so stop expecting further input.
                        negotiationState = NegotiationState.WAITING_FOR_IAC_OR_CHR;
                        session.getLogger().log(Level.INFO, "Received unknown telnet command: "
                                + TelnetByteTranslator.byteToString(by));
                }
                break;
            case WAITING_FOR_ARGUMENTS:
                //noinspection ConstantConditions
                if (tmpReceivedSequence.getCommandByte() == SB) {
                    if (by == IAC) negotiationState = NegotiationState.SUBNEGOTIATION_INTERRUPTED;
                    else tmpReceivedArguments.add(by);
                } else {
                    tmpReceivedSequence.setOptionByte(by);
                    // Finished
                    parseCompleteSequence();
                }
                break;
            case SUBNEGOTIATION_INTERRUPTED:
                switch (by) {
                    case IAC:
                        // IAC was escaped, so add it to the arguments and return to listening for arguments
                        tmpReceivedArguments.add(by);
                        negotiationState = NegotiationState.WAITING_FOR_ARGUMENTS;
                        break;
                    case SE:
                        // The sequence has completed
                        // Set the argument bytes
                        tmpReceivedSequence.setArgumentBytes(tmpReceivedArguments.toArray(new Byte[0]));

                        // Reset
                        tmpReceivedArguments = new ArrayList<>();
                        // Finished
                        parseCompleteSequence();
                        break;
                    default:
                        // Will throw an error, but not break out of subnegotiation. Only SE can do that.
                        throw new InvalidTelnetSequenceException("Subnegotiation was interrupted by " + by);
                }
                break;
        }
    }

    void parseCompleteSequence() throws IOException {
        // Parse
        if (tmpReceivedSequence.isValid()) {
            // Compare against registered triggers
            List<TelnetResponse> responseList;
            if (tmpReceivedSequence.getOptionByte() != null) {
                responseList = negotiationFlow.get(tmpReceivedSequence.getOptionByte());
            } else {
                responseList = negotiationFlow.get(tmpReceivedSequence.getCommandByte());
            }
            if (responseList == null) return;

            for (TelnetResponse response : responseList) {
                if (response != null) {
                    // Reset state before applying the response
                    negotiationState = NegotiationState.WAITING_FOR_IAC_OR_CHR;

                    // trigger (and response) found
                    response.run(tmpReceivedSequence);
                } else {
                    // No trigger or response found
                    session.getLogger().log(Level.INFO, "Unknown response for '"
                            + TelnetByteTranslator.bytesToString(tmpReceivedSequence.getByteSequence()) + "'");

                    // Inform the client that the server does not expect the client to perform,
                    // or that the server refuses to perform, the request.
                    if (tmpReceivedSequence.getOptionByte() != null) {
                        // IAC
                        writeToClient(IAC);
                        // The action, inverted so that it becomes negative indicating refusal
                        //noinspection ConstantConditions
                        if (tmpReceivedSequence.getCommandByte() == WILL) writeToClient(DONT);
                        else if (tmpReceivedSequence.getCommandByte() == DO) writeToClient(WONT);
                        // The option
                        writeToClient(tmpReceivedSequence.getOptionByte());
                    }
                }
            }
        } else {
            session.getLogger().log(Level.INFO, "Invalid sequence received");
        }

        // Reset sequence
        tmpReceivedSequence = new TelnetSequence();
    }

    //
    // --- MODE SETTERS ---
    //
    public void setCharset(@NotNull Charset charset) {
        if (supportedCharsets.contains(charset)) {
            session.getLogger().log(Level.INFO, "Changed charset to " + charset.name());
            currentCharset = charset;
        } else {
            throw new UnsupportedCharsetException(charset.name());
        }
    }

    public @NotNull Charset getCharset() {
        return currentCharset;
    }

    //
    // --- NEGOTIATION FLOW ---
    //
    // This method name... is rather, hm, verbose..
    public void sendSequenceWhenNotNegotiating(TelnetSequence sequence) throws IOException {
        if (sequence != null && sequence.isValid()) {
            pendingSequences.add(sequence);
            // Attempt to send
            sendPendingSequence();
        }
    }

    void sendPendingSequence() throws IOException {
        if (negotiationState != NegotiationState.WAITING_FOR_IAC_OR_CHR) return;

        synchronized (terminalWriteLock) {
            if (pendingSequences.size() == 0) return;
            TelnetSequence sequence = pendingSequences.remove(0);
            sequence.confirmValid();
            if (!sequence.isValid()) return;

            writeToClient(ArrayUtil.byteObjectsToBytes(sequence.getByteSequence()));
        }
    }

    public void registerNegotiationFlow(@NotNull Byte trigger, @NotNull TelnetResponse response) {
        synchronized (negotiationFlow) {
            List<TelnetResponse> responseList = negotiationFlow.get(trigger);
            if (responseList == null) {
                negotiationFlow.putIfAbsent(trigger, new ArrayList<>() {{
                    add(response);
                }});
            } else {
                responseList.add(response);
                // fixme maybe unnecessary?
                negotiationFlow.replace(trigger, responseList);
            }
        }
    }

    public void deregisterNegotiationFlow(@NotNull Byte trigger) {
        negotiationFlow.remove(trigger);
    }

    public void deregisterNegotiationFlow(@NotNull Byte trigger, @NotNull TelnetResponse response) {
        synchronized (negotiationFlow) {
            List<TelnetResponse> responseList = negotiationFlow.get(trigger);
            if (responseList != null) {
                responseList.remove(response);
                // fixme maybe unnecessary?
                negotiationFlow.replace(trigger, responseList);
            }
        }
    }

    //
    // --- BUILT-IN NEGOTIATIONS ---
    //
    /**
     * Attempts to negotiate for a specific charset to be used between the
     * client and server.
     * If the client refuses to negotiate then nothing will change,
     * and if the client agrees to negotiate then the server will ask if
     * it supports any of the charsets in the array. The first charset
     * provided in the array will be preferred, and the others treated as
     * fallback alternatives. If all fails, then nothing will change.
     *
     * For charset negotiation to be performed Binary Mode must
     * first be enabled, as many charsets tend to utilize bytes with
     * a value > 127, and binary mode provides full support up to 255.
     *
     * @param charsetArray An array of charsets. The ones after
     *                     the first one will be treated as fallback alternatives.
     */
    public void negotiateCharset(@NotNull Charset[] charsetArray) throws IOException {
        // Some constant values here to make the code easier to read.
        // There's more to the charset negotiation (42) than just this, but this is just a basic implementation of it.
        final byte REQUEST = (byte) 0x01;
        final byte ACCEPTED = (byte) 0x02;

        // Check for binary mode?
        if(!transmitBinaryEnabled) negotiateTransmitBinary(true);

        List<Charset> validCharsets = new ArrayList<>();
        StringBuilder validCharsetsString = new StringBuilder();

        // Filter charsetArray so that only supported charsets can be requested
        for (Charset charset:charsetArray) {
            // First ensure the charset is supported, then ensure it's not already added
            if(supportedCharsets.contains(charset) && !validCharsets.contains(charset)){
                validCharsets.add(charset);
                validCharsetsString.append(";").append(charset.name());
            }
        }

        // Prefix with 0x1 (option), and encode request string into bytes
        Byte[] requestBytes = ArrayUtil.mergeArrays(new Byte[]{REQUEST},
                ArrayUtil.bytesToByteObjects(validCharsetsString.toString().getBytes(ASCII_CHARSET))
        );

        // De-register any old negotiationFlow for charset handling, and
        // register a new, updated negotiationFlow for charset handling to
        // replace it with.
        deregisterNegotiationFlow(CHARSET);
        registerNegotiationFlow(CHARSET, trigger -> {
            // Don't attempt to parse an invalid trigger
            if(!trigger.isValid()) return;

            //noinspection ConstantConditions
            switch (trigger.getCommandByte()){
                // Just in case the client initiates the request
                case WILL:
                    // Reply to client
                    sendSequenceWhenNotNegotiating(new TelnetSequence(DO, CHARSET));
                    break;
                // Outgoing Subnegotiaton
                case DO:
                    // Reply to client
                    sendSequenceWhenNotNegotiating(new TelnetSequence(SB, CHARSET, requestBytes));
                    break;
                // Incoming Subnegotiation
                case SB:
                    // Parse
                    byte[] argBytes = ArrayUtil.byteObjectsToBytes(trigger.getArgumentBytes());

                    if(argBytes[0] == ACCEPTED){
                        byte[] argStringBytes = new byte[argBytes.length - 1];
                        if (argBytes.length - 2 >= 0){
                            System.arraycopy(argBytes, 1, argStringBytes, 0, argBytes.length - 2);
                        }
                        String argString = new String(
                                argStringBytes,
                                ASCII_CHARSET
                        );

                        for (Charset charset:charsetArray) {
                            // If there's a match, send a reply that we're now using that charset,
                            // and change the current charset to match
                            if(argString.contains(charset.name())){
                                Byte[] replyBytes = ArrayUtil.mergeArrays(new Byte[]{0x2},
                                        ArrayUtil.bytesToByteObjects(charset.name().getBytes(session.getCharset()))
                                );
                                sendSequenceWhenNotNegotiating(new TelnetSequence(SB, CHARSET, replyBytes));

                                // Change the current charset
                                setCharset(charset);
                                break;
                            }
                        }
                    }
                    break;
            }
        });
        // Ask the client politely if they wish to begin negotiating charsets
        sendSequenceWhenNotNegotiating(new TelnetSequence(WILL, CHARSET));
    }

    /**
     * Enables or disables client input being automatically echoed by the NVT.
     */
    @SuppressWarnings("SameParameterValue")
    void negotiateEcho(boolean enable) throws IOException {
        if(enable){
            deregisterNegotiationFlow(ECHO);
            registerNegotiationFlow(ECHO, trigger -> echoEnabled = false);

            // Ask the client politely that we will not echo for it
            sendSequenceWhenNotNegotiating(new TelnetSequence(WONT, ECHO));
        } else {
            deregisterNegotiationFlow(ECHO);
            registerNegotiationFlow(ECHO, trigger -> echoEnabled = true);

            // Ask the client politely that we will echo for it
            sendSequenceWhenNotNegotiating(new TelnetSequence(WILL, ECHO));
        }
    }

    /**
     * Enables or disables SUPPRESS_GO_AHEAD, resulting in either charbreak or linebreak operation.
     * With suppression enabled then each character input by the client will get
     * instantly echoed (charbreak), rather than it only being sent upon pressing enter (linebreak).
     */
    @SuppressWarnings({"ConstantConditions", "SameParameterValue"})
    void negotiateSuppressGoAhead(boolean enable) throws IOException {
        deregisterNegotiationFlow(SUPPRESS_GO_AHEAD);
        registerNegotiationFlow(SUPPRESS_GO_AHEAD, trigger -> {
            if(!trigger.isValid()) return;
            switch (trigger.getCommandByte()){
                case WILL:
                    sendSequenceWhenNotNegotiating(new TelnetSequence(DO, SUPPRESS_GO_AHEAD));
                case DO:
                    suppressGoAheadEnabled = true;
                    break;
                case WONT:
                case DONT:
                    suppressGoAheadEnabled = false;
                    break;
            }
        });

        // Ask the client politely to stop suppressing go ahead
        byte command;
        if(enable) command = WILL; else command = DONT;
        sendSequenceWhenNotNegotiating(new TelnetSequence(command, SUPPRESS_GO_AHEAD));
    }

    @SuppressWarnings("SameParameterValue")
    void negotiateTransmitBinary(boolean enable) throws IOException {
        if(!negotiationFlow.containsKey(TRANSMIT_BINARY)){
            registerNegotiationFlow(TRANSMIT_BINARY, trigger -> {
                if(!trigger.isValid()) return;
                //noinspection ConstantConditions
                switch (trigger.getCommandByte()) {
                    case WILL:
                        // Reply to client to inquiry more about this request
                        sendSequenceWhenNotNegotiating(new TelnetSequence(DO, TRANSMIT_BINARY));
                    case DO:
                        transmitBinaryEnabled = true;
                        break;
                    case DONT:
                        transmitBinaryEnabled = false;
                }
            });
        }

        if(enable){
            sendSequenceWhenNotNegotiating(new TelnetSequence(DO, TRANSMIT_BINARY));
            sendSequenceWhenNotNegotiating(new TelnetSequence(WILL, TRANSMIT_BINARY));
        } else {
            sendSequenceWhenNotNegotiating(new TelnetSequence(DONT, TRANSMIT_BINARY));
            sendSequenceWhenNotNegotiating(new TelnetSequence(WONT, TRANSMIT_BINARY));
        }
    }

    //
    // --- SOCKET I/O ---
    //

    /**
     * @return -1 if closed, otherwise a value from 0 - 255.
     */
    public byte readFromTerminal() throws IOException, TerminationException {
        // Unsigned (0 - 255)
        int by = inputStream.read();

        // ALWAYS check for disconnect
        if (by == -1) {
            throw new TerminationException("Connection terminated by client");
        }
        return (byte) by;
    }

    public void writeToClient(@NotNull String str) {
        try {
            writeToClient(str.getBytes(currentCharset));
        } catch (IOException ex) {
            session.getLogger().log(Level.WARNING, "Exception when writing to client", ex);
        }
    }

    public void writeToClient(byte outByte) throws IOException {
        synchronized (terminalWriteLock) {
            outputStream.write(outByte);
            outputStream.flush();
            //session.getLogger().log(Level.INFO, "Sent: '" + outByte + "' (" + TelnetByteTranslator.byteToString(outByte) + ")");
        }
    }

    public void writeToClient(byte[] outBytes) throws IOException {
        synchronized (terminalWriteLock) {
            outputStream.write(outBytes);
            outputStream.flush();
            //session.getLogger().log(Level.INFO, "Sent: '" + TelnetByteTranslator.byteToString(outByte) + "'");
        }
    }

    void writeToEngine(String decoded) throws IOException {
        // Check if the WM wishes to receive bytes, if yes then send.
        PrismaWM windowManager = session.getWindowManager();
        if (windowManager != null && windowManager.wantsInputAsStream() && toWindowManager != null) {
            toWindowManager.write(decoded.getBytes(session.getCharset()));
            toWindowManager.flush();
        }

        // Pass along the input
        inputParser.processDecoded(decoded);
    }
}
