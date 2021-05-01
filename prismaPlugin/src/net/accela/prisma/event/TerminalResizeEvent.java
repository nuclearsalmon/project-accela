package net.accela.prisma.event;

import net.accela.prisma.gui.geometry.Size;
import net.accela.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called when receiving mouse input
 */
public class TerminalResizeEvent extends Event {
    final @NotNull Size newSize;

    public TerminalResizeEvent(@NotNull Size newSize) {
        this.newSize = newSize;
    }

    public @NotNull Size getNewSize() {
        return newSize;
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s[newSize=%s]", this.getClass().getName(), newSize);
    }
}
