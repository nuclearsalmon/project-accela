package net.accela.prisma.exception;

/**
 * Thrown when attempting to interact with a closed WindowManager
 */
public class DeadWMException extends RuntimeException {
    /**
     * Constructs a new {@link DeadWMException}
     */
    public DeadWMException() {
        super();
    }
}
