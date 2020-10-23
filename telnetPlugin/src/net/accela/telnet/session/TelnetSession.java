package net.accela.telnet.session;

import net.accela.ansi.AnsiLib;
import net.accela.prisma.PrismaWM;
import net.accela.prisma.session.TextGraphicsSession;
import net.accela.server.AccelaAPI;
import net.accela.server.Server;
import net.accela.server.event.server.SessionAssignedEngineEvent;
import net.accela.telnet.server.TelnetSessionServer;
import net.accela.telnet.server.TelnetSocketServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class TelnetSession extends TextGraphicsSession {
    // Session IO
    final Socket socket;
    final TelnetSessionServer sessionServer;

    // Session writes, WindowManager reads. Non-lasting stream.
    PipedOutputStream outToWindowManager = null;
    PipedInputStream inToWindowManager = null;

    // WindowManager writes, Session reads. Non-lasting stream.
    PipedOutputStream outFromWindowManager = null;
    PipedInputStream inFromWindowManager = null;

    public TelnetSession(@NotNull final TelnetSocketServer telnetSocketServer,
                         @NotNull final Socket socket, @NotNull final UUID uuid) throws IOException {
        // Run the default constructor first
        super(telnetSocketServer, uuid);

        // Assign these values
        this.socket = socket;

        // Construct a sessionServer
        sessionServer = new TelnetSessionServer(this, socket);

        // Start the sessionServer
        sessionServer.start();
    }

    @Override
    public void writeToClient(@NotNull String str) {
        sessionServer.writeToClient(str);
    }

    @Override
    public void swapWM(@NotNull Class<? extends PrismaWM> engineClass) {
        final String exceptionString = "Exception when creating WindowManager instance";
        boolean success = true;
        // Attempt to instantiate a new WindowManager
        try {
            // Close the old WindowManager
            if (windowManager != null) windowManager.close();

            // Remove object references
            windowManager = null;
            sessionServer.setWindowManagerStreams(null, null);

            // Close old streams
            // To WindowManager from Session
            if (outToWindowManager != null) outToWindowManager.close();
            if (inToWindowManager != null) inToWindowManager.close();
            // To Session from WindowManager
            if (outFromWindowManager != null) outFromWindowManager.close();
            if (inFromWindowManager != null) inFromWindowManager.close();

            // Setup new streams
            // To WindowManager from Session
            outToWindowManager = new PipedOutputStream();
            inToWindowManager = new PipedInputStream(outToWindowManager);
            // To Session from WindowManager
            outFromWindowManager = new PipedOutputStream();
            inFromWindowManager = new PipedInputStream(outFromWindowManager);

            // Get a constructor
            Constructor<? extends PrismaWM> engineConstructor = engineClass.getConstructor(
                    TextGraphicsSession.class,
                    InputStream.class,
                    OutputStream.class
            );

            // Create a new instance
            windowManager = engineConstructor.newInstance(this, inToWindowManager, outFromWindowManager);

            // Set stream object to match the current ones
            sessionServer.setWindowManagerStreams(inFromWindowManager, outToWindowManager);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Exception when modifying IO streams for WindowManager swap", ex);
            success = false;
        } catch (InstantiationException ex) {
            getLogger().log(Level.SEVERE, exceptionString + " - failed to instantiate", ex);
            success = false;
        } catch (InvocationTargetException ex) {
            getLogger().log(Level.SEVERE, exceptionString + " - failed to invoke", ex);
            success = false;
        } catch (NoSuchMethodException ex) {
            getLogger().log(Level.SEVERE, exceptionString + " - no matching WindowManager constructor found", ex);
            success = false;
        } catch (IllegalAccessException ex) {
            getLogger().log(Level.SEVERE, exceptionString + " - could not access method", ex);
            success = false;
        }

        if (success) {
            // Announce that the Session has been assigned a new WindowManager instance
            AccelaAPI.getServer().getPluginManager().callEvent(
                    new SessionAssignedEngineEvent(this), Server.PRIVATE_CHANNEL
            );
        } else {
            // Retry, unless it's the default WindowManager implementation that failed,
            // in which case we should close the session
            if (!PrismaWM.class.equals(engineClass)) {
                swapWM(PrismaWM.class);
            } else {
                close("Failed WindowManager swap to default implementation");
            }
        }
    }

    @Override
    public @NotNull Charset getCharset() {
        return sessionServer.getCharset();
    }

    @Override
    public @NotNull List<Charset> getSupportedCharsets() {
        return sessionServer.supportedCharsets;
    }

    @Override
    public void setCharset(@NotNull Charset charset) throws UnsupportedCharsetException {
        if (getSupportedCharsets().contains(charset)) {
            sessionServer.setCharset(charset);
            getLogger().log(Level.INFO, "Set charset to '" + charset + "'.");
        } else throw new UnsupportedCharsetException(charset.toString());
    }

    /**
     * @return This session's socket
     */
    public @NotNull Socket getSocket() {
        return socket;
    }

    /**
     * Closes the session
     */
    @Override
    public void close(@Nullable String reason) {
        getLogger().log(Level.INFO, "Terminating session");

        // Reset terminal
        /* TODO: 10/21/20 This should be implemented in TelnetSessionServer instead.
         *   Make a close() method. */
        try {
            sessionServer.writeToClient((AnsiLib.CLR + AnsiLib.RIS).getBytes(getCharset()));
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Exception when writing terminal reset to client", ex);
        }
        // Interrupt the negotiation thread
        sessionServer.interrupt();

        // Attempt to close the WindowManager
        sessionServer.setWindowManagerStreams(null, null);
        try {
            if (windowManager != null) windowManager.close();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Exception when closing WM during the session shutdown process", ex);
        }

        // Attempt to close the WindowManager Streams
        try {
            if (inToWindowManager != null) inToWindowManager.close();
            if (outToWindowManager != null) outToWindowManager.close();
            if (inFromWindowManager != null) inFromWindowManager.close();
            if (outFromWindowManager != null) outFromWindowManager.close();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Exception when attempting to close WindowManager streams", ex);
        }

        // Attempt to close the socket
        try {
            socket.close();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Exception when attempting to close the socket");
        }

        // Perform the default closing actions
        super.close(reason);
    }

    @Override
    public void sendMessage(@NotNull String message) {

    }
}
