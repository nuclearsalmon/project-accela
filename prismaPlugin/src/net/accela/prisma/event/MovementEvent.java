package net.accela.prisma.event;

import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableIdentifier;
import net.accela.prisma.geometry.Rect;
import net.accela.server.event.Event;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Broadcast when a {@link Drawable}'s position or size changes.
 */
public class MovementEvent extends WMEvent {
    final DrawableIdentifier target;
    final Rect oldRect, newRect;

    /**
     * @param caller The {@link Plugin} that is instantiating this {@link Event}.
     * @param target The {@link DrawableIdentifier} that is about to become activated.
     *               If null, then all {@link Drawable}s are expected to be activated.
     */
    public MovementEvent(@NotNull Plugin caller,
                         @NotNull DrawableIdentifier target,
                         @NotNull Rect oldRect, @NotNull Rect newRect) {
        super(caller);
        this.target = target;
        this.oldRect = oldRect;
        this.newRect = newRect;
    }

    /**
     * @return The {@link Drawable} that is about to become activated.
     * If null, then all {@link Drawable}s are expected to be activated.
     */
    public @NotNull DrawableIdentifier getTarget() {
        return target;
    }

    public @NotNull Rect getOldRect() {
        return oldRect;
    }

    public @NotNull Rect getNewRect() {
        return newRect;
    }
}
