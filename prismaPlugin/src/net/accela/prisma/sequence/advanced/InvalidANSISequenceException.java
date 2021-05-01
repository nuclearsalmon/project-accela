package net.accela.prisma.sequence.advanced;

/**
 * When an ANSI sequence is constructed with a string that does not match the pattern.
 */
public class InvalidANSISequenceException extends Exception {
    String sequenceAsString;

    /**
     * Constructs a new InvalidANSISequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public InvalidANSISequenceException(final String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidANSISequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public InvalidANSISequenceException(final ANSISequence sequence, final String message) {
        super(message);
        this.sequenceAsString = sequence.toString();
    }

    /**
     * Constructs a new InvalidANSISequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public InvalidANSISequenceException(final CharSequence sequence, final String message) {
        super(message);
        this.sequenceAsString = sequence.toString();
    }

    /**
     * Constructs a new InvalidANSISequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public InvalidANSISequenceException(final CharSequence sequence, final String message, final Throwable throwable) {
        super(message, throwable);
        this.sequenceAsString = sequence.toString();
    }

    /**
     * Constructs a new InvalidANSISequenceException with the given message
     *
     * @param message Brief message explaining the cause of the exception
     */
    public InvalidANSISequenceException(final CharSequence sequence, final String message, final Exception exception) {
        super(message, exception);
        this.sequenceAsString = sequence.toString();
    }

    public String getSequenceAsString() {
        return sequenceAsString;
    }
}