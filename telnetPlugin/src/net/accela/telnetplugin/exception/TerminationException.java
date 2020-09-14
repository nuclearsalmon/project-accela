package net.accela.telnetplugin.exception;

/**
 * May be thrown in some cases when the session is terminated,
 * in order to interrupt any following IO ops from going haywire
 */
public class TerminationException extends Exception {
    /**
     * Constructs a new TerminationException based on the given Exception
     *
     * @param throwable Exception that triggered this Exception
     */
    public TerminationException(final Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructs a new TerminationException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public TerminationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new TerminationException based on the given Exception
     *
     * @param message Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    public TerminationException(final Throwable throwable, final String message) {
        super(message, throwable);
    }

    /**
     * Constructs a new TerminationException
     */
    public TerminationException() {

    }
}
