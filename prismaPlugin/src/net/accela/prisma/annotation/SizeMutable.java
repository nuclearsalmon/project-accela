package net.accela.prisma.annotation;

import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface SizeMutable extends SizeReadable {
    /**
     * @param size The size of this.
     */
    void setSize(@NotNull Size size) throws NodeNotFoundException;

    /**
     * @param width  The width of this.
     * @param height The height of this.
     */
    default void setSize(int width, int height) throws NodeNotFoundException {
        setSize(new Size(width, height));
    }

    /**
     * @param width The width of this.
     */
    default void setWidth(@Range(from = 1, to = Integer.MAX_VALUE) int width) {
        setSize(width, getHeight());
    }

    /**
     * @param height The height of this.
     */
    default void setHeight(@Range(from = 1, to = Integer.MAX_VALUE) int height) {
        setSize(getWidth(), height);
    }
}
