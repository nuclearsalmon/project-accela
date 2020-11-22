package net.accela.prisma.exception;

import net.accela.prisma.PrismaWM;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;

/**
 * Thrown when attempting to use {@link PrismaWM} without it having a plugin instance to reference to.
 * In short, it needs to know the instance that is representing itself in plugin form.
 */
public class MissingPluginInstanceException extends RuntimeException {
    /**
     * Constructs a new {@link RectOutOfBoundsException}
     */
    public MissingPluginInstanceException() {
        super();
    }
}