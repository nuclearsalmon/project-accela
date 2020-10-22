package net.accela.ansi.exception;

import net.accela.ansi.sequence.ESCSequence;
import org.jetbrains.annotations.Nullable;

/**
 * A somewhat broad exception meant to be used for ANSI sequence exceptions.
 */
public class ESCSequenceException extends RuntimeException {
    String sequenceAsString;

    /**
     * Constructs a new ESCSequenceException based on the given Exception
     *
     * @param throwable Exception that triggered this Exception
     */
    public ESCSequenceException(final Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructs a new ESCSequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public ESCSequenceException(final String message) {
        super(message);
    }

    /**
     * Constructs a new ESCSequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public ESCSequenceException(final ESCSequence sequence, final String message) {
        super(message);
        this.sequenceAsString = sequence.toString();
    }

    /**
     * Constructs a new ESCSequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public ESCSequenceException(final CharSequence sequence, final String message) {
        super(message);
        this.sequenceAsString = sequence.toString();
    }

    /**
     * Constructs a new ESCSequenceException based on the given Exception
     *
     * @param message   Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    public ESCSequenceException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new ESCSequenceException based on the given Exception
     *
     * @param message   Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    public ESCSequenceException(final ESCSequence sequence, final String message, final Throwable throwable) {
        super(message, throwable);
        this.sequenceAsString = sequence.toString();
    }

    /**
     * Constructs a new ESCSequenceException based on the given Exception
     *
     * @param message   Brief message explaining the cause of the exception
     * @param throwable Exception that triggered this Exception
     */
    public ESCSequenceException(final CharSequence sequence, final String message, final Throwable throwable) {
        super(message, throwable);
        this.sequenceAsString = sequence.toString();
    }

    public @Nullable String getSequenceAsString() {
        return sequenceAsString;
    }
}
