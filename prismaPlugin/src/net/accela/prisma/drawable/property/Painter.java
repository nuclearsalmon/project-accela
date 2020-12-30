package net.accela.prisma.drawable.property;

import net.accela.prisma.Drawable;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Rect;
import org.jetbrains.annotations.NotNull;

/**
 * Anything that can paint {@link Drawable}s
 */
public interface Painter {
    /**
     * Draws the contents of the requested {@link Drawable}, as well as any intersecting {@link Drawable}'s.
     *
     * @param drawable the {@link Drawable} to draw.
     */
    default void paint(@NotNull Drawable drawable) throws NodeNotFoundException {
        paint(drawable.getRelativeRect());
    }

    /**
     * Draws the contents of the requested {@link Drawable}, as well as any intersecting {@link Drawable}'s.
     *
     * @param rect the {@link Rect} to draw.
     */
    void paint(@NotNull Rect rect) throws NodeNotFoundException;
}
