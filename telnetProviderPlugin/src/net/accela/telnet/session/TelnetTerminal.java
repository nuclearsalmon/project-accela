package net.accela.telnet.session;

import net.accela.prismatic.terminal.ModernTerminal;
import net.accela.prismatic.ui.geometry.Point;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;

public class TelnetTerminal extends ModernTerminal {
    final Socket socket;
    final TelnetNegotiator negotiator;

    public TelnetTerminal(final @NotNull TelnetSession session,
                          final @NotNull Socket socket) throws IOException {
        this(new TelnetNegotiator(socket, session), session, socket);
    }

    private TelnetTerminal(final @NotNull TelnetNegotiator negotiator,
                           final @NotNull TelnetSession session,
                           final @NotNull Socket socket) throws IOException {
        super(session, negotiator.getInputStream(), negotiator.getOutputStream(), UTF8_CHARSET);
        this.socket = socket;
        this.negotiator = negotiator;

        // Start negotiating basic functionality
        negotiator.clientEchoNegotiation(true);
        negotiator.suppressGoAheadNegotiation(false);
        negotiator.transmitBinaryNegotiation(true);
        negotiator.extendedAsciiNegotiation(true);
        negotiator.resizeNotificationNegotiation(true);
        negotiator.setLineMode0Negotiation();
        negotiator.charsetNegotiation();

        // Test for unicode charset support
        testUnicode();
    }

    /**
     * Warning: Potentially destructive operation.
     * <p>
     * Clears the terminal and tests for unicode support,
     * then switches to either UTF-8 or IBM_437 depending on the test result.
     */
    private synchronized void testUnicode() {
        // todo non-destructive operation
        //  - save and restore buffer
        //  - save and restore cursor position
        // todo migrate this method to one of the parent classes
        synchronized (this) {
            try {
                // Prepare and clear
                setCharsetWithoutNegotiation(UTF8_CHARSET);
                saveCursorPosition();
                resetCursorPosition();

                // Run support test
                writeToTerminal("ΔΩÔ".getBytes(UTF8_CHARSET));
                Point curPos = getCursorPosition();

                // Restore terminal
                clear();
                restoreCursorPosition();

                // Apply charset
                boolean supportsUnicode = curPos.getX() == 3 && curPos.getY() == 0;
                if (!supportsUnicode) super.setCharset(IBM437_CHARSET);
            } catch (IOException e) {
                new IOException("Failed unicode support detection.", e).printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Unlike normal charset switching, it will attempt negotiation with the client.
     *
     * @param charset The {@link Charset} to switch to.
     * @throws IOException
     */
    @Override
    public void setCharset(@NotNull Charset charset) throws IOException {
        negotiator.charsetNegotiation();
    }

    void setCharsetWithoutNegotiation(@NotNull Charset charset) throws IOException {
        super.setCharset(charset);
    }
}
