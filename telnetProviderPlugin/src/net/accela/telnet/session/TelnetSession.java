package net.accela.telnet.session;

import net.accela.prismatic.Prismatic;
import net.accela.prismatic.session.TextGraphicsSession;
import net.accela.server.AccelaAPI;
import net.accela.server.Server;
import net.accela.server.event.server.SessionAssignedEngineEvent;
import net.accela.telnet.TelnetSocketServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Level;

public final class TelnetSession extends TextGraphicsSession {
    // Session IO
    final Socket socket;

    // StreamTerminal configuration
    protected final TelnetTerminal terminal;

    public TelnetSession(@NotNull final TelnetSocketServer telnetSocketServer,
                         @NotNull final Socket socket, @NotNull final UUID uuid) throws IOException {
        // Run the default constructor first
        super(telnetSocketServer, uuid);

        // Assign these values
        this.socket = socket;

        // Configure terminal
        terminal = new TelnetTerminal(this, socket);
    }

    @Override
    public void swapWM(@NotNull Class<? extends Prismatic> engineClass) {
        final String exceptionString = "Exception when creating WindowManager instance";
        boolean success = true;
        // Attempt to instantiate a new WindowManager
        try {
            // Close the deprecated WindowManager
            if (windowManager != null) {
                try {
                    windowManager.close();
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Failed to close window manager", e);
                } finally {
                    // Remove object reference
                    windowManager = null;
                }
            }

            // Get a constructor
            Constructor<? extends Prismatic> engineConstructor = engineClass.getConstructor(TextGraphicsSession.class);

            // Create a new instance
            windowManager = engineConstructor.newInstance(this);
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
            AccelaAPI.getPluginManager().callEvent(
                    new SessionAssignedEngineEvent(this), Server.PRIVATE_CHANNEL
            );
        } else {
            // Retry, unless it's the default WindowManager implementation that failed,
            // in which case we should close the session
            if (!Prismatic.class.equals(engineClass)) {
                swapWM(Prismatic.class);
            } else {
                close("Failed WindowManager swap to default implementation");
            }
        }
    }

    @Override
    public @NotNull TelnetTerminal getTerminal() {
        return terminal;
    }

    /**
     * @return This session's socket
     */
    public @NotNull Socket getSocket() {
        return socket;
    }

    /**
     * Closes the session with an optional message describing the reason for closing.
     * How this message is used is up to the SessionCreator implementation to decide.
     *
     * @param reason The reason for closing.
     */
    @Override
    public void close(@Nullable String reason) {
        getLogger().log(Level.INFO, "Terminating session");

        // Perform the default closing actions
        super.close(reason);

        // Attempt to close the socket
        try {
            socket.close();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Exception when attempting to close the socket");
        }
    }

    @Override
    public void sendMessage(@NotNull String message) {

    }
}
