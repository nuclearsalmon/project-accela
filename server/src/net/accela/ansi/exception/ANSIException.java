package net.accela.ansi.exception;

/**
 * A broad exception meant to be used for any kind of ANSI-related issues,
 * provided there's no dedicated exception created for said error.
 */
public class ANSIException extends Exception {
    /**
     * Constructs a new ANSIException based on the given Exception
     *
     * @param throwable Exception that triggered this Exception
     */
    public ANSIException(final Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructs a new ANSIException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public ANSIException(final String message) {
        super(message);
    }

    /**
     * Constructs a new ANSIException based on the given Exception
     *
     * @param message   Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    public ANSIException(final Throwable throwable, final String message) {
        super(message, throwable);
    }
}
