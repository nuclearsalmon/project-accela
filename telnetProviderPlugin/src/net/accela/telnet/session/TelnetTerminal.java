package net.accela.telnet.session;

import net.accela.prisma.terminal.ModernTerminal;
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
    }

    @Override
    public void setCharset(@NotNull Charset charset) throws IOException {
        if (supportedCharsets.contains(charset)) {
            synchronized (supportedCharsets) {
                supportedCharsets.remove(charset);
                supportedCharsets.add(0, charset);
            }
        }
        negotiator.charsetNegotiation();
    }

    void setCharsetInternal(@NotNull Charset charset) {
        this.charset = charset;
        reader.setCharset(charset);
    }
}
