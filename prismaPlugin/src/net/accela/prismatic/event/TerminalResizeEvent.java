package net.accela.prismatic.event;

import net.accela.prismatic.ui.geometry.Size;
import net.accela.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Fires in a {@link net.accela.prismatic.session.TextGraphicsSession}'s channel
 * when its {@link net.accela.prismatic.terminal.Terminal} has been resized.
 * Please query the {@link net.accela.prismatic.terminal.Terminal} for information on its current size.
 */
public class TerminalResizeEvent extends Event {
    final @NotNull Size newSize;

    public TerminalResizeEvent(@NotNull Size newSize) {
        super(true);
        this.newSize = newSize;
    }

    public @NotNull Size getSize() {
        return newSize;
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s[newSize=%s]", this.getClass().getName(), newSize);
    }
}
