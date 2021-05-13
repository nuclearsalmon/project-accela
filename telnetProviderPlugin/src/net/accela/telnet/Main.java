package net.accela.telnet;

import net.accela.server.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    private final List<TelnetSocketServer> telnetSocketServers = new ArrayList<>();

    @Override
    public void onEnable() {
        // Enable copying default values if there aren't any.
        getConfig().options().copyDefaults(true);
        // Save these defaults if needed
        saveConfig();

        // Get default port
        int port = getConfig().getInt("servers." + telnetSocketServers.size() + ".port");

        // Create a new server
        try {
            createServer(port);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to create server", ex);
        }
    }

    @Override
    public void onDisable() {
        destroyAll();
    }

    public void createServer(int port) throws IOException {
        // Register it
        TelnetSocketServer telnetSocketServer;
        synchronized (telnetSocketServers) {
            telnetSocketServer = new TelnetSocketServer(this, String.valueOf(telnetSocketServers.size()), port);
            telnetSocketServers.add(telnetSocketServer);
        }

        // Start it
        telnetSocketServer.runTaskAsynchronously(this);
    }

    public void destroyServer(String name) {
        if (name == null) return;
        synchronized (telnetSocketServers) {
            for (TelnetSocketServer telnetSocketServer : telnetSocketServers) {
                if (telnetSocketServer.getName().equals(name)) {
                    telnetSocketServer.close();
                    telnetSocketServers.remove(telnetSocketServer);
                    break;
                }
            }
        }
    }

    public void destroyAll() {
        synchronized (telnetSocketServers) {
            for (TelnetSocketServer telnetSocketServer : telnetSocketServers) {
                telnetSocketServer.close();
                telnetSocketServers.remove(telnetSocketServer);
            }
        }
    }
}
