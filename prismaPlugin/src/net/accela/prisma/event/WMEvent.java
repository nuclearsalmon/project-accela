package net.accela.prisma.event;

import net.accela.server.event.Event;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A simple interface for tagging all UI-related events
 */
public abstract class WMEvent extends Event {
    final @NotNull Plugin caller;

    public WMEvent(@NotNull Plugin caller) {
        super(true);
        this.caller = caller;
    }

    public WMEvent(@NotNull Plugin caller, boolean async) {
        super(async);
        this.caller = caller;
    }

    public @NotNull Plugin getCaller() {
        return caller;
    }
}
