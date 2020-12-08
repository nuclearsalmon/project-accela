package net.accela.telnet.server;

import net.accela.telnet.exception.InvalidTelnetSequenceException;
import net.accela.telnet.util.TelnetBytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.accela.telnet.util.TelnetBytes.*;

public final class TelnetSequence {
    private Byte commandByte;
    private Byte optionByte;
    private Byte[] argumentBytes;

    public TelnetSequence() {
    }

    public TelnetSequence(@NotNull Byte commandByte) {
        this.commandByte = commandByte;
    }

    public TelnetSequence(@NotNull Byte commandByte, @NotNull Byte optionByte) {
        this.commandByte = commandByte;
        this.optionByte = optionByte;
    }

    public TelnetSequence(@NotNull Byte commandByte, @NotNull Byte optionByte, @NotNull Byte[] argumentBytes) {
        this.commandByte = commandByte;
        this.optionByte = optionByte;
        this.argumentBytes = argumentBytes;
    }

    @Nullable
    public Byte getCommandByte() {
        return commandByte;
    }

    @Nullable
    public Byte getOptionByte() {
        return optionByte;
    }

    @Nullable
    public Byte[] getArgumentBytes() {
        return argumentBytes;
    }

    /**
     * Will not set the commandByte if one is already present
     */
    public void setCommandByte(@NotNull Byte commandByte) {
        if (this.commandByte == null) this.commandByte = commandByte;
    }

    /**
     * Will not set the optionByte if one is already present
     */
    public void setOptionByte(@NotNull Byte optionByte) {
        if (this.optionByte == null) this.optionByte = optionByte;
    }

    /**
     * Will not set the argumentBytes if one is already present
     */
    public void setArgumentBytes(Byte[] argumentBytes) {
        if (this.argumentBytes == null && argumentBytes.length > 0) this.argumentBytes = argumentBytes;
    }

    /**
     * Prefixes with IAC automatically and combines all the bytes present into a valid sequence.
     * Also suffixes any SB statement with IAC SE.
     *
     * @return A valid telnet sequence in bytes, or null if it is invalid.
     */
    @Nullable
    public Byte[] getByteSequence() throws InvalidTelnetSequenceException {
        confirmValid();

        // Base sequence, prefixed with IAC
        Byte[] result = new Byte[]{IAC};

        // Add the commandByte
        result = Arrays.copyOf(result, result.length + 1);
        result[result.length - 1] = commandByte;

        // Add the optionByte (if any)
        result = Arrays.copyOf(result, result.length + 1);
        result[result.length - 1] = optionByte;

        // Add the argumentBytes (if any)
        if (argumentBytes != null && argumentBytes.length != 0) {
            int beginning = result.length;

            // Extend the array size
            result = Arrays.copyOf(result, result.length + argumentBytes.length - 1);

            // Append modifier bytes
            for (byte by : argumentBytes) result[beginning++] = by;
        }

        // Suffix with IAC SE (if necessary)
        if (commandByte == SB) {
            result = Arrays.copyOf(result, result.length + 1);
            result[result.length - 2] = IAC;
            result[result.length - 1] = SE;
        }

        return result;
    }

    /**
     * Validates the sequence.
     * See {@link TelnetSequence#confirmValid()} if you wish to inquire more about why the sequence is invalid.
     *
     * @return true if this TelnetSequence is valid.
     */
    public boolean isValid() {
        try {
            confirmValid();
            return true;
        } catch (InvalidTelnetSequenceException ignored) {
            return false;
        }
    }

    /**
     * Validates the sequence.
     * This method may throw an exception with a short message describing why the sequence is invalid.
     * See {@link TelnetSequence#isValid()} if you wish to validate but not throw any exceptions.
     */
    public void confirmValid() throws InvalidTelnetSequenceException {
        if (commandByte == null) {
            throw new InvalidTelnetSequenceException("A commandByte is required");
        }

        switch (commandByte) {
            case SB:
                if (argumentBytes == null || argumentBytes.length < 1) {
                    throw new InvalidTelnetSequenceException("Too few arguments");
                }
            case WILL:
            case WONT:
            case DO:
            case DONT:
                if (optionByte == null) {
                    throw new InvalidTelnetSequenceException("An optionByte is required");
                }
                break;
        }
    }


    @Override
    public String toString() {
        return TelnetBytes.bytesToString(this);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getByteSequence());
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return this.getByteSequence().equals(o);
    }
}
