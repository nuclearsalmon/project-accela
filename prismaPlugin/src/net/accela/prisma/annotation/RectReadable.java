package net.accela.prisma.annotation;

import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.geometry.Size;
import org.jetbrains.annotations.NotNull;

public interface RectReadable extends SizeReadable, PointReadable {
    /**
     * @return A {@link Rect} representing the size of this {@link Drawable},
     * with the values of {@link Rect#getStartPoint()} returning [0, 0].
     * The same result can be achieved using {@link Rect#zero()},
     * which is what this uses internally.
     */
    @NotNull
    default Rect getZeroRect() throws NodeNotFoundException {
        return getRelativeRect().zero();
    }

    /**
     * @return The size and relative position of this {@link Drawable}.
     */
    @NotNull Rect getRelativeRect() throws NodeNotFoundException;

    /**
     * @return The size and absolute position of this {@link Drawable}.
     */
    @NotNull
    Rect getAbsoluteRect() throws NodeNotFoundException;

    /**
     * @return The size of this {@link Drawable}.
     */
    default @NotNull Size getSize() throws NodeNotFoundException {
        return getRelativeRect().getSize();
    }
}
