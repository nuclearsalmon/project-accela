package net.accela.telnetplugin.server;

import net.accela.telnetplugin.exception.InvalidTelnetSequenceException;
import net.accela.telnetplugin.exception.TerminationException;
import net.accela.telnetplugin.util.TelnetByteTranslator;
import net.accela.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static net.accela.telnetplugin.util.TelnetBytes.*;

public final class TelnetSessionServer extends Thread {
    // The session this TelnetSessionServer is serving
    final TelnetSession session;
    final InputStream inputStream;
    final OutputStream outputStream;
    final InputStream fromEngine;
    final OutputStream toEngine;
    final Thread fromEngineReaderThread;

    // Charset configuration
    public final static Charset UTF_8_CHARSET = StandardCharsets.UTF_8;
    public final static Charset UTF_16_CHARSET = StandardCharsets.UTF_16;
    public final static Charset ASCII_CHARSET = StandardCharsets.US_ASCII;
    public final static Charset IBM437_CHARSET = Charset.forName("437");
    public final List<Charset> supportedCharsets = new ArrayList<>(){{
        add(UTF_8_CHARSET);
        add(UTF_16_CHARSET);
        add(ASCII_CHARSET);
        add(IBM437_CHARSET);
    }};

    /**
     * The current state of negotiation
     */
    enum NegotiationState {
        NOT_NEGOTIATING,
        WAITING_FOR_COMMAND,
        WAITING_FOR_ARGUMENTS,
        SUBNEGOTIATION_INTERRUPTED
    }
    @NotNull NegotiationState negotiationState = NegotiationState.NOT_NEGOTIATING;

    // Synchronization lock
    final Object terminalWriteLock = new Object();

    // Flags
    boolean echoEnabled = true;
    boolean suppressGoAheadEnabled = false;
    boolean transmitBinaryEnabled = false;

    // The Key is the received byte trigger (not a full sequence), and the Value is a list of responses
    final HashMap<Byte, List<Response>> negotiationFlow = new HashMap<>();

    // Sequences pending to be sent
    // When adding a sequence to be sent, you may also supply it with a few response sequences,
    // that get registered in 'negotiations'. A response may change a TelnetOption in the TelnetSession.
    final List<TelnetSequence> pendingSequences = new ArrayList<>();

    // A temporary cache of the sequence being received from the client
    TelnetSequence tmpReceivedSequence = new TelnetSequence();
    // A temporary cache of the sequence arguments being received from the client
    List<Byte> tmpReceivedArguments = new ArrayList<>();

    public TelnetSessionServer(@NotNull TelnetSession session,
                               InputStream inputStream,
                               OutputStream outputStream,
                               InputStream fromEngine,
                               OutputStream toEngine){
        this.session = session;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.fromEngine = fromEngine;
        this.toEngine = toEngine;

        // Register default negotiation for logout
        registerNegotiationFlow(LOGOUT, fullTrigger -> {
            sendSequenceWhenNotNegotiating(new TelnetSequence(WILL, LOGOUT));
            session.close("The client requested logout");
        });

        // Start the reader thread
        fromEngineReaderThread = new Thread(() -> {
            Thread.currentThread().setName(TelnetSession.class.getSimpleName()
                    + ":" + session.getUUID() + ":fromEngineReader");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    int read = fromEngine.read();
                    if (read == -1) break;
                    writeToTerminal((byte) read);
                }
            }
            catch (IOException ex) {
                session.getLogger().log(Level.WARNING, "Exception when reading from engine: ", ex);
                // Stop the TelnetSessionServer too
                this.interrupt();
            }
        });
        fromEngineReaderThread.start();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(TelnetSessionServer.class.getSimpleName() + ":" + session.getUUID());

        try {
            // Start negotiating basic functionality
            negotiateEcho(false);
            negotiateSuppressGoAhead(true);
            negotiateTransmitBinary(true);
            negotiateCharset(supportedCharsets.toArray(new Charset[0]));

            // Start reader thread
            while (!isInterrupted()) {
                sendPendingSequence();

                // Proceed to parse
                byte command = (byte) readFromTerminal();

                if(negotiationState == NegotiationState.NOT_NEGOTIATING){
                    if(command == IAC){
                        negotiationState = NegotiationState.WAITING_FOR_COMMAND;
                    } else {
                        parseCharacter(command);
                    }
                } else {
                    parseNegotiation(command);
                }
            }
        } catch (IOException e){
            if(!isInterrupted()) session.getLogger().log(Level.WARNING, "IOException in TelnetSessionServer", e);
        } catch (TerminationException ignored){}

        fromEngineReaderThread.interrupt();
    }

    //
    // --- PARSING ---
    //
    void parseCharacter(byte by) throws IOException, TerminationException {
        Charset sessionCharset = session.getCharset();
        // Figure out how many bytes to read
        if (sessionCharset.equals(UTF_8_CHARSET)) {
            /*
                Binary    Hex          Comments
                0xxxxxxx  0x00..0x7F   Only byte of a 1-byte character encoding
                10xxxxxx  0x80..0xBF   Continuation byte: one of 1-3 bytes following the first
                110xxxxx  0xC0..0xDF   First byte of a 2-byte character encoding
                1110xxxx  0xE0..0xEF   First byte of a 3-byte character encoding
                11110xxx  0xF0..0xF7   First byte of a 4-byte character encoding
            */
            byte[] charBytes;
            int numberOfBytes = 1;
            if (by >= (byte) 0xC0 && by <= (byte) 0xDF) numberOfBytes = 2;
            else if (by <= (byte) 0xEF) numberOfBytes = 3;
            else if (by <= (byte) 0xF7) numberOfBytes = 4;

            charBytes = new byte[numberOfBytes];
            charBytes[0] = by;

            for (int i = 1; i < numberOfBytes; i++) {
                charBytes[i] = (byte) readFromTerminal();
            }

            writeToEngine(charBytes);
        } else if (sessionCharset.equals(UTF_16_CHARSET)) {
            byte[] charBytes = new byte[4];
            charBytes[0] = by;
            charBytes[1] = (byte) readFromTerminal();

            ByteBuffer bb = ByteBuffer.allocate(2);
            bb.put(charBytes[0]);
            bb.put(charBytes[1]);
            int tmp = bb.getInt();

            if(tmp >= 0xD800 && tmp <= 0xDBFF){
                charBytes[2] = (byte) readFromTerminal();
                charBytes[3] = (byte) readFromTerminal();
            }

            writeToEngine(charBytes);
        } else if(sessionCharset.equals(ASCII_CHARSET) || sessionCharset.equals(IBM437_CHARSET)){
            writeToEngine(by);
        } else {
            throw new UnsupportedCharsetException(sessionCharset.name());
        }
    }

    void parseNegotiation(byte command) throws IOException {
        switch (negotiationState){
            case WAITING_FOR_COMMAND:
                tmpReceivedSequence.setCommandByte(command);
                switch (command){
                    case SB:
                    case WILL:
                    case WONT:
                    case DO:
                    case DONT:
                        negotiationState = NegotiationState.WAITING_FOR_ARGUMENTS;
                        break;
                    case IP:
                        parseReceivedSequence();
                        negotiationState = NegotiationState.NOT_NEGOTIATING;
                        break;
                    case IAC:
                        break;
                    default:
                        // Did not receive a valid command, so stop expecting further input.
                        negotiationState = NegotiationState.NOT_NEGOTIATING;
                        session.getLogger().log(Level.INFO, "Received unknown telnet command: "
                                + TelnetByteTranslator.byteToString(command));
                }
                break;
            case WAITING_FOR_ARGUMENTS:
                //noinspection ConstantConditions
                if(tmpReceivedSequence.getCommandByte() == SB){
                    if(command == IAC) negotiationState = NegotiationState.SUBNEGOTIATION_INTERRUPTED;
                    else tmpReceivedArguments.add(command);
                } else {
                    tmpReceivedSequence.setOptionByte(command);
                    // Finished
                    parseReceivedSequence();
                }
                break;
            case SUBNEGOTIATION_INTERRUPTED:
                switch (command){
                    case IAC:
                        tmpReceivedArguments.add(command);
                        negotiationState = NegotiationState.WAITING_FOR_ARGUMENTS;
                        break;
                    case SE:
                        tmpReceivedSequence.setArgumentBytes(tmpReceivedArguments.toArray(new Byte[0]));

                        // Reset
                        tmpReceivedArguments = new ArrayList<>();
                        // Finished
                        parseReceivedSequence();
                        break;
                    default:
                        // Will throw an error, but not break out of subnegotiation. Only SE can do that.
                        throw new InvalidTelnetSequenceException("Subnegotiation was interrupted by " + command);
                }
                break;
        }
    }

    @SuppressWarnings("ConstantConditions")
    void parseReceivedSequence() throws IOException {
        // Parse
        if(tmpReceivedSequence.isValid()){
            // Compare against registered triggers
            List<Response> responseList;
            if(tmpReceivedSequence.getOptionByte() != null){
                responseList = negotiationFlow.get(tmpReceivedSequence.getOptionByte());
            } else {
                responseList = negotiationFlow.get(tmpReceivedSequence.getCommandByte());
            }
            if(responseList == null) return;

            for (Response response:responseList) {
                if(response != null){
                    // Reset state before applying the response
                    negotiationState = NegotiationState.NOT_NEGOTIATING;

                    // trigger (and response) found
                    response.run(tmpReceivedSequence);
                } else {
                    // No trigger or response found
                    session.getLogger().log(Level.INFO, "Unknown response for '"
                            + TelnetByteTranslator.bytesToString(tmpReceivedSequence.getByteSequence()) + "'");

                    // Inform the client that the server does not expect the client to perform,
                    // or that the server refuses to perform, the request.
                    if(tmpReceivedSequence.getOptionByte() != null){
                        writeToTerminal(IAC);
                        if(tmpReceivedSequence.getCommandByte() == WILL) writeToTerminal(DONT);
                        else if(tmpReceivedSequence.getCommandByte() == DO) writeToTerminal(WONT);
                        writeToTerminal(tmpReceivedSequence.getOptionByte());
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
    void setCharset(Charset charset){
        if(supportedCharsets.contains(charset)){
            session.getLogger().log(Level.INFO, "Changed charset to " + charset.name());
            session.setCharset(charset);
        }
    }

    //
    // --- NEGOTIATION FLOW ---
    //

    // This method name... is rather, hm, verbose..
    public void sendSequenceWhenNotNegotiating(TelnetSequence sequence) throws IOException {
        if(sequence != null && sequence.isValid()){
            pendingSequences.add(sequence);
            // Attempt to send
            sendPendingSequence();
        }
    }

    void sendPendingSequence() throws IOException {
        if(negotiationState != NegotiationState.NOT_NEGOTIATING) return;

        synchronized (terminalWriteLock){
            if(pendingSequences.size() == 0) return;
            TelnetSequence sequence = pendingSequences.remove(0);
            sequence.confirmValid();
            if(!sequence.isValid()) return;

            writeToTerminal(ArrayUtil.byteObjectsToBytes(sequence.getByteSequence()));
        }
    }

    public void registerNegotiationFlow(@NotNull Byte trigger, @NotNull Response response){
        synchronized (negotiationFlow){
            List<Response> responseList = negotiationFlow.get(trigger);
            if(responseList == null){
                negotiationFlow.putIfAbsent(trigger, new ArrayList<>() {{ add(response); }});
            } else {
                responseList.add(response);
                // fixme maybe unnecessary?
                negotiationFlow.replace(trigger, responseList);
            }
        }
    }

    public void deregisterNegotiationFlow(@NotNull Byte trigger){
        negotiationFlow.remove(trigger);
    }
    public void deregisterNegotiationFlow(@NotNull Byte trigger, @NotNull Response response){
        synchronized (negotiationFlow){
            List<Response> responseList = negotiationFlow.get(trigger);
            if(responseList != null){
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
     * Unsigned (0 - 255
     * @return -1 if closed, otherwise a value from 0 - 255.
     */
    public int readFromTerminal() throws IOException, TerminationException {
        int by = inputStream.read();
        //session.getLogger().log(Level.INFO, "Received: '" + TelnetByteTranslator.byteToString((byte) by) + "'");

        // ALWAYS check for disconnect
        if (by == -1) {
            session.close("Connection terminated by client");
            throw new TerminationException();
        }
        return by;
    }

    public void writeToTerminal(byte outByte) throws IOException {
        synchronized (terminalWriteLock){
            outputStream.write(outByte);
            outputStream.flush();
            //session.getLogger().log(Level.INFO, "Sent: '" + outByte + "' (" + TelnetByteTranslator.byteToString(outByte) + ")");
        }
    }
    
    public void writeToTerminal(byte[] outBytes) throws IOException {
        synchronized (terminalWriteLock){
            outputStream.write(outBytes);
            outputStream.flush();
            //session.getLogger().log(Level.INFO, "Sent: '" + TelnetByteTranslator.byteToString(outByte) + "'");
        }
    }

    void writeToEngine(byte outByte) throws IOException {
        toEngine.write(outByte);
        toEngine.flush();
    }

    void writeToEngine(byte[] outBytes) throws IOException {
        toEngine.write(outBytes);
        toEngine.flush();
    }
}
