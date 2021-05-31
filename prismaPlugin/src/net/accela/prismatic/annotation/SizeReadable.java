package net.accela.prismatic.annotation;

import net.accela.prismatic.Drawable;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.geometry.Size;
import org.jetbrains.annotations.NotNull;

public interface SizeReadable {
    /**
     * @return The size of this {@link Drawable}.
     */
    @NotNull Size getSize();

    /**
     * @return The width of this {@link Drawable}.
     * @see Rect#getWidth()
     * @see Size#getWidth()
     */
    default int getWidth() {
        return getSize().getWidth();
    }

    /**
     * @return The height of this {@link Drawable}.
     * @see Rect#getHeight()
     * @see Size#getHeight()
     */
    default int getHeight() {
        return getSize().getHeight();
    }

    /**
     * @return The capacity (width * height) of this {@link Drawable}.
     * @see Rect#getCapacity()
     * @see Size#getCapacity()
     */
    default int getCapacity() {
        return getSize().getCapacity();
    }
}
