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

    public TelnetTerminal(@NotNull TelnetSession session, @NotNull Socket socket) throws IOException {
        this(new TelnetNegotiator(socket, session), session, socket);
    }

    private TelnetTerminal(@NotNull TelnetNegotiator negotiator,
                           @NotNull TelnetSession session,
                           @NotNull Socket socket) throws IOException {
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

        // Figure out charset support
        // Test for unicode charset support
        try {
            // Prep and clear
            super.setCharset(UTF8_CHARSET);
            resetCursorPosition();

            // Run support test
            writeToTerminal("ΔΩÔ".getBytes(UTF8_CHARSET));
            Point curPos = getCursorPosition();

            // Restore terminal
            resetColorAndSGR();
            clear();
            resetCursorPosition();

            // Apply charset
            //fixme remove print dbg
            System.out.println("CHARTEST POS: " + curPos);
            boolean supportsUnicode = curPos.getX() == 3 && curPos.getY() == 0;
            if (!supportsUnicode) super.setCharset(IBM437_CHARSET);
        } catch (IOException e) {
            new IOException("Failed to test charset", e).printStackTrace();
        }
    }

    @Override
    public void setCharset(@NotNull Charset charset) throws IOException {
        // fixme dangerous
        negotiator.charsetNegotiation();
    }

    void setCharsetInternal(@NotNull Charset charset) throws IOException {
        super.setCharset(charset);
    }
}
