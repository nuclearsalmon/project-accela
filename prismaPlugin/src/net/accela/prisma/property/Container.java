package net.accela.prisma.property;

import net.accela.prisma.Drawable;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Containers contain {@link Drawable}'s.
 */
public interface Container {
    //
    // Container methods
    //

    /**
     * Attaches a {@link Drawable} to this {@link Container}
     *
     * @param drawable The {@link Drawable} to attach
     * @throws RectOutOfBoundsException when the rect is invalid
     */
    void attach(@NotNull Drawable drawable, @NotNull Plugin plugin) throws RectOutOfBoundsException, NodeNotFoundException;

    /**
     * Detaches a {@link Drawable} from this {@link Container}
     */
    void detach(@NotNull Drawable drawable) throws NodeNotFoundException;

    //
    // Painting
    //

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
