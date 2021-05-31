package net.accela.prismatic.annotation;

import net.accela.prismatic.Drawable;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.geometry.Size;
import org.jetbrains.annotations.NotNull;

public interface RectReadable extends SizeReadable, PointReadable {
    /**
     * @return A {@link Rect} representing the size of this {@link Drawable},
     * with the values of {@link Rect#getStartPoint()} returning [0, 0].
     * The same result can be achieved using {@link Rect#zero()},
     * which is what this uses internally.
     */
    @NotNull
    default Rect getZeroRect() {
        return getRelativeRect().zero();
    }

    /**
     * @return The size and relative position of this {@link Drawable}.
     */
    @NotNull Rect getRelativeRect();

    /**
     * @return The size and absolute position of this {@link Drawable}.
     */
    @NotNull
    Rect getAbsoluteRect();

    /**
     * @return The size of this {@link Drawable}.
     */
    default @NotNull Size getSize() {
        return getRelativeRect().getSize();
    }
}
