package net.accela.prisma.annotation;

import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.geometry.Size;
import org.jetbrains.annotations.NotNull;

public interface SizeReadable {
    /**
     * @return The size of this {@link Drawable}.
     */
    @NotNull Size getSize() throws NodeNotFoundException;

    /**
     * @return The width of this {@link Drawable}.
     * @see Rect#getWidth()
     * @see Size#getWidth()
     */
    default int getWidth() throws NodeNotFoundException {
        return getSize().getWidth();
    }

    /**
     * @return The height of this {@link Drawable}.
     * @see Rect#getHeight()
     * @see Size#getHeight()
     */
    default int getHeight() throws NodeNotFoundException {
        return getSize().getHeight();
    }

    /**
     * @return The capacity (width * height) of this {@link Drawable}.
     * @see Rect#getCapacity()
     * @see Size#getCapacity()
     */
    default int getCapacity() throws NodeNotFoundException {
        return getSize().getCapacity();
    }
}
