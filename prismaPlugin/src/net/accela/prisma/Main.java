package net.accela.prisma;

import net.accela.server.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    // Necessary for registering listeners later from the window manager
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }
}
