package net.accela.telnet.session;

import net.accela.prismatic.terminal.Terminal;
import net.accela.telnet.Main;
import net.accela.telnet.util.TelnetBytes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;

import static net.accela.telnet.util.TelnetBytes.*;

public class TelnetNegotiator {
    // I/O
    private final Socket socket;
    private final TelnetInputStream telnetInputStream;
    private final OutputStream outputStream;

    private final TelnetSession session;

    // Negotiation result flags
    private boolean clientEcho = true;
    private boolean suppressGoAhead = false;
    private boolean extendedAscii = false;
    private boolean clientLineMode0 = false;
    private boolean clientResizeNotification = false;

    public TelnetNegotiator(@NotNull Socket socket, @NotNull TelnetSession session) throws IOException {
        this.socket = socket;
        this.session = session;
        this.telnetInputStream = new TelnetInputStream(this, socket);
        this.outputStream = socket.getOutputStream();
    }

    //
    // Getters
    //

    public @NotNull OutputStream getOutputStream() {
        return outputStream;
    }

    public @NotNull InputStream getInputStream() {
        return telnetInputStream;
    }

    public @NotNull TelnetSession getSession() {
        return session;
    }

    // Flags

    public boolean clientEcho() {
        return clientEcho;
    }

    public boolean suppressGoAhead() {
        return suppressGoAhead;
    }

    public boolean extendedAscii() {
        return extendedAscii;
    }

    public boolean clientLineMode0() {
        return clientLineMode0;
    }

    public boolean isResizeNotify() {
        return clientResizeNotification;
    }

    //
    // Negotiation transmissions
    //

    public void charsetNegotiation() throws IOException {
        writeToTerminal(IAC, WILL, CHARSET);
    }

    public void suppressGoAheadNegotiation(boolean enable) throws IOException {
        writeToTerminal(IAC, enable ? WILL : WONT, SUPPRESS_GA);
    }

    public void transmitBinaryNegotiation(boolean enable) throws IOException {
        writeToTerminal(IAC, enable ? WILL : WONT, TRANSMIT_BINARY);
    }

    public void extendedAsciiNegotiation(boolean enable) throws IOException {
        writeToTerminal(IAC, enable ? WILL : WONT, EXTENDED_ASCII);
    }

    public void clientEchoNegotiation(boolean enable) throws IOException {
        writeToTerminal(IAC, enable ? WILL : WONT, ECHO);
    }

    public void resizeNotificationNegotiation(boolean enable) throws IOException {
        writeToTerminal(IAC, enable ? WILL : WONT, NAWS);
    }

    public void setLineMode0Negotiation() throws IOException {
        writeToTerminal(IAC, DO, LINEMODE, IAC, SB, LINEMODE, (byte) 1, (byte) 0, IAC, SE);
    }

    //
    // Negotiation parsing
    //

    void parseSequence(TelnetSequence sequence) throws IOException {
        switch (sequence.option) {
            case LOGOUT:
                logoutNegotiation(sequence);
                break;
            case CHARSET:
                charsetNegotiation(sequence);
                break;
            case SUPPRESS_GA:
                suppressGoAheadNegotiation(sequence);
                break;
            case EXTENDED_ASCII:
                extendedAsciiNegotiation(sequence);
                break;
            case ECHO:
                echoNegotiation(sequence);
                break;
            case LINEMODE:
                linemodeNegotiation(sequence);
                break;
            case NAWS:
                nawsNegotiation(sequence);
                break;
            default:
                switch (sequence.command) {
                    case DO:
                    case DONT:
                        onUnsupportedRequestCommand(sequence.command == DO, sequence.option);
                        break;
                    case WILL:
                    case WONT:
                        onUnsupportedStateCommand(sequence.command == WILL, sequence.option);
                        break;
                    case SB:
                        onUnsupportedSubnegotiation(sequence.option, sequence.arguments);
                    default:
                        throw new UnsupportedOperationException(String.format(
                                "No command handler implemented for %s",
                                TelnetBytes.byteToString(sequence.command)
                        ));
                }
                break;
        }
    }

    private void logoutNegotiation(TelnetSequence sequence) throws IOException {
        writeToTerminal(new TelnetSequence(WILL, LOGOUT));
        session.close("Client requested logout");
    }

    private void charsetNegotiation(TelnetSequence sequence) throws IOException {
        // Ensure we're investigating the correct option
        if (sequence.option != CHARSET)
            throw new IllegalArgumentException("Option " + sequence.option + " != charset");

        final byte command = sequence.command;
        final byte option = sequence.option;
        final byte[] arguments = sequence.arguments;

        // Some constant values here to make the code easier to read.
        // There's more to the charset negotiation (42) than just this, but this is just a basic implementation of it.
        final byte REQUEST = (byte) 0x01;
        final byte ACCEPTED = (byte) 0x02;

        switch (command) {
            // Just in case the client initiates the request
            case WILL:
                // Reply to client that yes we want to perform charset negotiation
                writeToTerminal(new TelnetSequence(DO, CHARSET));
                break;
            // Just in case the client initiates the request
            case DO:
                StringBuilder payload = new StringBuilder();

                // Filter charsetArray so that only supported charsets can be requested
                for (Charset charset : session.getTerminal().getSupportedCharsets()) {
                    // First ensure the charset is supported, then ensure it's not already added
                    if (session.getTerminal().getSupportedCharsets().contains(charset) &&
                            !payload.toString().contains(charset.name())) {
                        payload.append(charset.name()).append(" ");
                    }
                }
                payload.deleteCharAt(payload.length() - 1);

                // Prefix with 0x1 (option), and encode request string into bytes
                ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[]{REQUEST});
                byteBuffer.put(payload.toString().getBytes(Terminal.ASCII_CHARSET));

                writeToTerminal(new TelnetSequence(SB, CHARSET, byteBuffer.array()));
                break;
            // Incoming Subnegotiation
            case SB:
                if (arguments[0] == ACCEPTED) {
                    // Slice away the ACCEPTED byte, that's not a part of the string
                    byte[] stringArguments = new byte[arguments.length - 1];
                    System.arraycopy(arguments, 1, stringArguments, 0, arguments.length - 1);

                    String argString = new String(stringArguments, Terminal.ASCII_CHARSET);
                    session.getTerminal().setCharsetWithoutNegotiation(Charset.forName(argString));
                }
                break;
            default:
                onUnsupportedCommandForOption(command, option);
        }
    }

    private void suppressGoAheadNegotiation(TelnetSequence sequence) throws IOException {
        // Ensure we're investigating the correct option
        if (sequence.option != SUPPRESS_GA)
            throw new IllegalArgumentException("Option " + sequence.option + " != SUPPRESS_GO_AHEAD");

        final byte command = sequence.command;
        final byte option = sequence.option;
        final byte[] arguments = sequence.arguments;

        switch (command) {
            case DO, DONT -> {
                suppressGoAhead = (command == DO);
                replyBooleanNegotiation(command == DO, option);
            }
            default -> onUnsupportedCommandForOption(command, option);
        }
    }

    private void extendedAsciiNegotiation(TelnetSequence sequence) throws IOException {
        // Ensure we're investigating the correct option
        if (sequence.option != EXTENDED_ASCII)
            throw new IllegalArgumentException("Option " + sequence.option + " != EXTENDED_ASCII");

        final byte command = sequence.command;
        final byte option = sequence.option;
        final byte[] arguments = sequence.arguments;

        switch (command) {
            case DO, DONT -> {
                extendedAscii = (command == DO);
                replyBooleanNegotiation(command == DO, option);
            }
            default -> onUnsupportedCommandForOption(command, option);
        }
    }

    private void echoNegotiation(TelnetSequence sequence) {
        // Ensure we're investigating the correct option
        if (sequence.option != ECHO)
            throw new IllegalArgumentException("Option " + sequence.option + " != ECHO");

        final byte command = sequence.command;
        final byte option = sequence.option;
        final byte[] arguments = sequence.arguments;

        switch (command) {
            case WILL, WONT -> clientEcho = (command == WILL);
            default -> onUnsupportedCommandForOption(command, option);
        }
    }

    private void linemodeNegotiation(TelnetSequence sequence) {
        // Ensure we're investigating the correct option
        if (sequence.option != LINEMODE)
            throw new IllegalArgumentException("Option " + sequence.option + " != LINEMODE");

        final byte command = sequence.command;
        final byte option = sequence.option;
        final byte[] arguments = sequence.arguments;

        switch (command) {
            case WILL, WONT -> clientLineMode0 = (command == WILL);
            default -> onUnsupportedCommandForOption(command, option);
        }
    }

    private void nawsNegotiation(TelnetSequence sequence) {
        // Ensure we're investigating the correct option
        if (sequence.option != NAWS) throw new IllegalArgumentException("Option != NAWS");

        final byte command = sequence.command;
        final byte option = sequence.option;
        final byte[] arguments = sequence.arguments;

        switch (command) {
            case WILL, WONT -> clientResizeNotification = (command == WILL);
            case SB -> {
                // Bad argument length
                if (arguments.length != 4) {
                    throw new UnsupportedOperationException(String.format(
                            "Subnegotiaton for option %s requires 4 arguments",
                            TelnetBytes.byteToString(option)
                    ));
                }

                // Resize the terminal accordingly
                session.getTerminal().onResized(
                        convertTwoBytesToInt2(arguments[1], arguments[0]),
                        convertTwoBytesToInt2(arguments[3], arguments[2])
                );
            }
            default -> onUnsupportedCommandForOption(command, option);
        }
    }

    private static int convertTwoBytesToInt2(byte b1, byte b2) {
        return ((b2 & 0xFF) << 8) | (b1 & 0xFF);
    }

    //
    // Error messages
    //

    private void onUnsupportedStateCommand(boolean enabling, byte option) {
        Main.getPlugin(Main.class).getLogger().log(Level.WARNING, String.format(
                "Unsupported operation: Client %s do %s",
                (enabling ? "will" : "won't"),
                TelnetBytes.byteToString(option)
        ));
    }

    private void onUnsupportedRequestCommand(boolean askedToDo, byte option) {
        Main.getPlugin(Main.class).getLogger().log(Level.WARNING, String.format(
                "Unsupported request: Client asked to %s %s",
                (askedToDo ? "do" : "don't"),
                TelnetBytes.byteToString(option)
        ));
    }

    private void onUnsupportedSubnegotiation(byte option, byte[] arguments) {
        Main.getPlugin(Main.class).getLogger().log(Level.WARNING, String.format(
                "Unsupported subnegotiation: Client sent %s with subnegotiation data %s",
                TelnetBytes.byteToString(option),
                TelnetBytes.bytesToString(arguments)
        ));
    }

    private void onUnsupportedCommandForOption(byte command, byte option) {
        Main.getPlugin(Main.class).getLogger().log(Level.WARNING, String.format(
                "No command handler implemented for option %s with command %s",
                TelnetBytes.byteToString(option),
                TelnetBytes.byteToString(command)
        ));
    }

    //
    // Output
    //

    public void replyBooleanNegotiation(boolean will, byte option) throws IOException {
        writeToTerminal(IAC, will ? WILL : WONT, option);
    }

    public void writeToTerminal(@NotNull TelnetSequence sequence) throws IOException {
        writeToTerminal(sequence.getByteSequence());
    }

    /**
     * Writes bytes to the client terminal and flushes.
     *
     * @param bytes bytes to be written
     * @throws IOException If an exception occurs
     */
    public void writeToTerminal(byte... bytes) throws IOException {
        if (bytes.length <= 0) return;
        synchronized (outputStream) {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }
}
