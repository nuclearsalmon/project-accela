package net.accela.telnetplugin.server;

import net.accela.ansigraphics.EngineConstructor;
import net.accela.ansigraphics.Prism;
import net.accela.server.AccelaAPI;
import net.accela.server.Server;
import net.accela.server.event.server.SessionReadyEvent;
import net.accela.server.session.GraphicsSession;
import net.accela.server.session.provider.SocketServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;

public final class TelnetSession extends GraphicsSession {
    // Session IO
    TelnetSessionServer telnetSessionServer;

    public TelnetSession(@NotNull final TelnetSocketServer telnetSocketServer,
                         @NotNull final Socket socket) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Run the default constructor first
        super(telnetSocketServer, socket);

        // Install an engine
        try {
            swapEngine(Prism.class);
        } catch (Exception ex) {
            if(ex instanceof IOException){
                getLogger().log(Level.SEVERE, "Exception when modifying IO streams for the Engine", ex);
            } else if(ex instanceof NoSuchMethodException){
                getLogger().log(Level.SEVERE, "No such engine constructor", ex);
            } else if(ex instanceof IllegalAccessException |
                    ex instanceof InstantiationException |
                    ex instanceof InvocationTargetException){
                getLogger().log(Level.SEVERE, "Exception when creating Engine instance", ex);
            }
            close("Exception when creating engine");
            throw ex;
        }

        AccelaAPI.getServer().getPluginManager().callEvent(new SessionReadyEvent(this), Server.PRIVATE_CHANNEL);
    }

    @Override
    public void swapEngine(Class<? extends EngineConstructor> engineClass) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // Close the old engine
        if(engine != null) engine.close();

        // Interrupt the TelnetSessionServer as the streams are about to change soon
        if(telnetSessionServer != null){
            telnetSessionServer.interrupt();
        }

        // Close the old streams and setup new ones

            // This is necessary as the pipe might be closed, and thus we should always replace it
            // To engine from the session
            if(pipeFromSession != null) pipeFromSession.close();
            if(pipeToEngine != null) pipeToEngine.close();
            pipeToEngine = new PipedOutputStream();
            pipeFromSession = new PipedInputStream(pipeToEngine);

            // To session from the engine
            if(pipeFromEngine != null) pipeFromEngine.close();
            if(pipeToSession != null) pipeToSession.close();
            pipeToSession = new PipedOutputStream();
            pipeFromEngine = new PipedInputStream(pipeToSession);


        // Setup a TelnetSessionServer with the new streams and start it
        this.telnetSessionServer = new TelnetSessionServer(this,
                rawInputStream, rawOutputStream, pipeFromEngine, pipeToEngine
        );
        telnetSessionServer.start();

        // Construct a new engine
        // Get a constructor
        Constructor<? extends EngineConstructor> engineConstructor = engineClass.getConstructor(
                GraphicsSession.class,
                InputStream.class,
                OutputStream.class
        );
        engine = engineConstructor.newInstance(this, pipeFromSession, pipeToSession);
    }

    /**
     * Closes the session
     */
    @Override
    public void close(@Nullable String reason) {
        // Interrupt the negotiation thread
        telnetSessionServer.interrupt();

        // Perform the default closing actions
        super.close(reason);
    }

    @Override
    public @NotNull List<Charset> getSupportedCharsets() {
        return telnetSessionServer.supportedCharsets;
    }

    @Override
    public void sendMessage(@NotNull String message) {

    }

    public SocketServer getSubServer(){
        return (SocketServer) getCreator();
    }
}
