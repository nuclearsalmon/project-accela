package net.accela.prisma.gui.geometry.exception;

import net.accela.prisma.gui.geometry.Rect;

/**
 * Thrown when an invalid {@link Rect} being instantiated or used.
 */
public class RectOutOfBoundsException extends RuntimeException {
    /**
     * Constructs a new {@link RectOutOfBoundsException}
     */
    public RectOutOfBoundsException() {
        super();
    }

    /**
     * Constructs a new {@link RectOutOfBoundsException} based on the given {@link Exception}
     *
     * @param throwable The {@link Throwable} that triggered this {@link Exception}
     */
    public RectOutOfBoundsException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructs a new {@link RectOutOfBoundsException} with the given message
     *
     * @param message A brief message explaining the cause of the {@link Exception}
     */
    public RectOutOfBoundsException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link RectOutOfBoundsException} based on the given {@link Exception}
     *
     * @param throwable The {@link Throwable} that triggered this {@link Exception}
     * @param message   A brief message explaining the cause of the {@link Exception}
     */
    public RectOutOfBoundsException(Throwable throwable, String message) {
        super(message, throwable);
    }
}
