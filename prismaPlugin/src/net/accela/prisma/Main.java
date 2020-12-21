package net.accela.prisma;

import net.accela.server.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    //
    // Debug Flags
    //

    /**
     * Whether to focus a Drawable when it gets attached to {@link PrismaWM}
     */
    public static final boolean DBG_FOCUS_ON_WM_ATTACHMENT = true;

    /**
     * Whether to focus a Drawable when it gets attached to a {@link DrawableContainer}
     */
    public static final boolean DBG_FOCUS_ON_DRAWABLE_CONTAINER_ATTACHMENT = true;

    /**
     * Whether to throw exception when out of terminal bounds or not.
     * Disabling it makes the logs more easily readable.
     */
    public static final boolean DBG_RESPECT_TERMINAL_BOUNDS = false;

    /**
     * Whether to throw exception when out of container bounds or not.
     * Disabling it makes the logs more easily readable.
     */
    public static final boolean DBG_RESPECT_CONTAINER_BOUNDS = true;

    //
    // Miscellaneous
    //

    // This is necessary for registering listeners later from the window manager
    private static Main instance;

    @Override
    public void onEnable() {
        // todo move to constructor?
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }
}
