package net.accela.prismatic.annotation;

import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Rect;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Anything that can paint {@link Drawable}s
 */
public interface ItemPainter {
    /**
     * Draws the contents of the requested {@link Drawable}, as well as any intersecting {@link Drawable}'s.
     *
     * @param drawable the {@link Drawable} to draw.
     */
    default void paint(@NotNull Drawable drawable) throws NodeNotFoundException, IOException {
        paint(drawable.getRelativeRect());
    }

    /**
     * Draws the contents of the requested {@link Drawable}, as well as any intersecting {@link Drawable}'s.
     *
     * @param rect the {@link Rect} to draw.
     */
    void paint(@NotNull Rect rect) throws NodeNotFoundException, IOException;
}
