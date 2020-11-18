package net.accela.sessionIntroducer;

import net.accela.prisma.PrismaWM;
import net.accela.prisma.session.TextGraphicsSession;
import net.accela.server.AccelaAPI;
import net.accela.server.Server;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.event.server.SessionCreatedEvent;
import net.accela.server.plugin.PluginManager;
import net.accela.server.plugin.java.JavaPlugin;
import net.accela.server.session.Session;

public class Main extends JavaPlugin {
    final Listener sessionCreatedListener = new SessionCreatedListener();

    @Override
    public void onEnable() {
        PluginManager pluginManager = AccelaAPI.getPluginManager();

        // Register listeners
        pluginManager.registerEvents(sessionCreatedListener, this, Server.PRIVATE_CHANNEL);
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

                // Try swapping WindowManager
                graphicsSession.swapWM(PrismaWM.class);
            }
        }
    }
}
