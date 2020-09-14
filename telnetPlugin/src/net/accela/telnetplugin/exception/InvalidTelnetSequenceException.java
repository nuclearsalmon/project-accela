package net.accela.telnetplugin.exception;

import net.accela.telnetplugin.server.TelnetSequence;

/**
 * Thrown when attempting to use a {@link TelnetSequence} that's invalid.
 */
public class InvalidTelnetSequenceException extends RuntimeException {
    /**
     * Constructs a new InvalidTelnetSequenceException based on the given
     * Exception
     *
     * @param throwable Exception that triggered this Exception
     */
    public InvalidTelnetSequenceException(final Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructs a new InvalidTelnetSequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public InvalidTelnetSequenceException(final String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidTelnetSequenceException based on the given
     * Exception
     *
     * @param message Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    public InvalidTelnetSequenceException(final Throwable throwable, final String message) {
        super(message, throwable);
    }

    /**
     * Constructs a new InvalidTelnetSequenceException
     */
    public InvalidTelnetSequenceException() {

    }
}
