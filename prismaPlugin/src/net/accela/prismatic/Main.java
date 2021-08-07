package net.accela.prismatic;

import net.accela.prismatic.session.TextGraphicsSession;
import net.accela.server.AccelaAPI;
import net.accela.server.Server;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.event.server.SessionCreatedEvent;
import net.accela.server.plugin.PluginManager;
import net.accela.server.plugin.java.JavaPlugin;
import net.accela.server.session.Session;

public class Main extends JavaPlugin {
    //
    // Debug Flags
    //
    // todo migrate these into Prismatic.java

    /**
     * Whether to focus a Drawable when it gets attached to {@link Prismatic}
     */
    public static final boolean DBG_FOCUS_ON_WM_ATTACHMENT = true;

    /**
     * Whether to throw exception when out of low level bounds or not.
     * Disabling it makes the logs more easily readable.
     */
    public static final boolean DBG_RESPECT_TERMINAL_BOUNDS = false;

    /**
     * Whether to throw exception when out of container bounds or not.
     * Disabling it makes the logs more easily readable.
     */
    public static final boolean DBG_RESPECT_CONTAINER_BOUNDS = false;


    //
    // Main
    //

    // Reference to event listener
    private static final Listener sessionCreatedListener = new SessionCreatedListener();

    // This is necessary for registering listeners later from the window manager
    private static Main pluginInstance;

    @Override
    public void onEnable() {
        // Store the plugin instance, we need this in order to call events.
        pluginInstance = this;

        // Register listeners
        PluginManager pluginManager = AccelaAPI.getPluginManager();
        pluginManager.registerEvents(sessionCreatedListener, this, Server.PRIVATE_CHANNEL);
    }

    public static Main getPluginInstance() {
        return pluginInstance;
    }

    @Override
    public void onDisable() {
        // Unregister listeners
        AccelaAPI.getPluginManager().unregisterEvents(sessionCreatedListener);
    }

    static class SessionCreatedListener implements Listener {
        @EventHandler
        public void onSessionCreated(SessionCreatedEvent event) {
            Session baseSession = event.getSession();

            if (baseSession instanceof TextGraphicsSession) {
                TextGraphicsSession graphicsSession = (TextGraphicsSession) baseSession;

                // Try injecting Prismatic into the TextGraphicsSession
                graphicsSession.swapWM(Prismatic.class);
            }
        }
    }
}
