package net.accela.prisma.terminal.old;

import net.accela.prisma.event.PluginRequiredEvent;
import net.accela.server.event.Event;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Simple {@link Event} to be used when receiving input
 */
@Deprecated
public abstract class InputEvent extends PluginRequiredEvent {
    public InputEvent(@NotNull Plugin caller) {
        super(caller);
    }
}
