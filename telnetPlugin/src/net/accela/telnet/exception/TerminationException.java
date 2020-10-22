package net.accela.telnet.exception;

import org.jetbrains.annotations.NotNull;

/**
 * May be thrown in some cases when the session is terminated,
 * in order to interrupt any following IO operations and prevent them from going haywire
 */
public class TerminationException extends Exception {
    /**
     * Constructs a new TerminationException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public TerminationException(final @NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new TerminationException based on the given Exception
     *
     * @param message   Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    public TerminationException(final Throwable throwable, @NotNull final String message) {
        super(message, throwable);
    }

    @Override
    public @NotNull String getMessage() {
        String message = super.getMessage();
        return message == null ? "" : message;
    }
}
