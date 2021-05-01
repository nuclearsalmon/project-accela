package net.accela.prisma.event;

import net.accela.server.event.Event;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A simple interface for tagging all UI-related events
 */
public abstract class PluginRequiredEvent extends Event {
    final @NotNull Plugin caller;

    public PluginRequiredEvent(@NotNull Plugin caller) {
        super(true);
        this.caller = caller;
    }

    public PluginRequiredEvent(@NotNull Plugin caller, boolean async) {
        super(async);
        this.caller = caller;
    }

    public @NotNull Plugin getCaller() {
        return caller;
    }
}
