package net.accela.telnetplugin.server;

import net.accela.server.AccelaAPI;
import net.accela.server.Server;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.event.server.SessionClosedEvent;
import net.accela.server.plugin.java.JavaPlugin;
import net.accela.server.session.provider.SocketServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TelnetSocketServer extends SocketServer {
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
        while (!Thread.interrupted()){
            try {
                // Accept connection
                Socket clientSocket = serverSocket.accept();

                // Register it as a new session
                TelnetSession session;
                try {
                    session = new TelnetSession(this, clientSocket);
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    getLogger().log(Level.SEVERE, "Exception when creating session", e);
                    // Don't add the session or do anything with it
                    continue;
                }

                telnetSessions.add(session);

                getLogger().log(Level.INFO, "Assigned connection '"
                        + clientSocket.getRemoteSocketAddress().toString()
                        + "' to session '" + session.UUID + "'"
                );
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, "Exception when accepting connection", ex);
            }
        }

        // Close server when done
        close();
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
                        + "'@" + session.getSocket().getInetAddress().getAddress().toString()
                        + " closed due to the following reason: '" + event.getReason() + "'");
            }
        }
    }
}
