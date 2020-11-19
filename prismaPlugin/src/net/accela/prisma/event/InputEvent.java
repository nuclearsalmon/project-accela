package net.accela.prisma.event;

import net.accela.server.event.Event;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Simple {@link Event} to be used when receiving input
 */
public abstract class InputEvent extends WMEvent {
    public InputEvent(@NotNull Plugin caller) {
        super(caller);
    }
}
