package net.accela.prisma.gui.drawabletree;

import net.accela.prisma.gui.Drawable;

/**
 * Thrown when attempting to use a {@link Drawable} without it having a {@link net.accela.prisma.gui.drawabletree.Node}
 * associated with it. Typically this means the {@link Drawable} is not attached.
 */
public class NodeNotFoundException extends RuntimeException {
    /**
     * Constructs a new {@link NodeNotFoundException}
     */
    public NodeNotFoundException() {
        super();
    }

    /**
     * Constructs a new {@link NodeNotFoundException}
     *
     * @param message A brief message explaining the cause of the {@link Exception}
     */
    public NodeNotFoundException(String message) {
        super(message);
    }
}