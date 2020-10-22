package net.accela.telnet.server;

import net.accela.server.AccelaAPI;
import net.accela.server.Server;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.event.server.SessionClosedEvent;
import net.accela.server.event.server.SessionCreatedEvent;
import net.accela.server.plugin.java.JavaPlugin;
import net.accela.server.session.provider.SessionCreator;
import net.accela.telnet.session.TelnetSession;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class TelnetSocketServer extends SessionCreator {
    final ServerSocket serverSocket;
    final List<TelnetSession> telnetSessions = new ArrayList<>();

    public TelnetSocketServer(final JavaPlugin plugin, String name, int port) throws IOException {
        super(plugin, name);

        // Register a socket
        ServerSocket tmpServerSocket;
        try {
            tmpServerSocket = new ServerSocket(port);
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Exception when attempting to bind to port " + port, ex);
            throw ex;
        }
        serverSocket = tmpServerSocket;

        // Register events
        AccelaAPI.getServer().getPluginManager().registerEvents(
                new SessionEventListener(this),
                getPlugin(),
                Server.PRIVATE_CHANNEL
        );
    }

    @Override
    public void run() {
        Thread.currentThread().setName("TelnetSocketServer-" + getName());

        getLogger().log(Level.INFO, "Telnet server '" + getName() + "' started on port " + serverSocket.getLocalPort());

        // Main thread
        while (!Thread.interrupted()) {
            try {
                // Accept connection
                Socket clientSocket = serverSocket.accept();

                // Register it as a new session.
                // This is multithreaded for two reasons - stability and performance.
                // This way, if the instantiation throws an exception it doesn't crash the entire TelnetSocketServer.
                new SessionThread(this, clientSocket).start();
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Exception when accepting connection", ex);
            }
        }

        // Close server when done
        close();
    }

    class SessionThread extends Thread {
        final TelnetSocketServer socketServer;
        final Socket clientSocket;

        public SessionThread(@NotNull TelnetSocketServer socketServer, @NotNull Socket clientSocket) {
            this.socketServer = socketServer;
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            // UUID
            UUID uuid = java.util.UUID.randomUUID();
            // Logging
            getLogger().log(Level.INFO, "Assigned connection '"
                    + clientSocket.getRemoteSocketAddress().toString()
                    + "' to session UUID '" + uuid + "'"
            );
            // Instantiate the session
            TelnetSession session;
            try {
                session = new TelnetSession(socketServer, clientSocket, uuid);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Exception when instantiating new TelnetSession", ex);
                return;
            }
            telnetSessions.add(session);

            // Send a SessionCreatedEvent
            AccelaAPI.getServer().getPluginManager().callEvent(
                    new SessionCreatedEvent(session), Server.PRIVATE_CHANNEL
            );
        }
    }

    @Override
    public void close() {
        // First close all sessions
        for (TelnetSession session:telnetSessions) {
            telnetSessions.remove(session);
            if(session.isAlive()) session.close("Provider closed");
        }

        // Then close the ServerSocket, freeing the port
        try {
            serverSocket.close();
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Exception when closing server socket", ex);
        }
    }

    class SessionEventListener implements Listener {
        final TelnetSocketServer socketServer;

        public SessionEventListener(@NotNull TelnetSocketServer socketServer){
            this.socketServer = socketServer;
        }

        @EventHandler
        public void onSessionClosed(@NotNull SessionClosedEvent event){
            if(event.getSession() instanceof TelnetSession){
                TelnetSession session = ((TelnetSession) event.getSession());
                if(session.getCreator() == socketServer) telnetSessions.remove(session);

                getLogger().log(Level.INFO, "Session '" + session.getUUID()
                        + "'@" + Arrays.toString(session.getSocket().getInetAddress().getAddress())
                        + " closed due to the following reason: '" + event.getReason() + "'");
            }
        }
    }
}
