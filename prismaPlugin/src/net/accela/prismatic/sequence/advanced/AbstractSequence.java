package net.accela.prismatic.sequence.advanced;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class AbstractSequence {
    @NotNull
    protected String sequenceString;

    //
    // Constructor
    //

    AbstractSequence(@NotNull String str) throws InvalidANSISequenceException {
        this.sequenceString = str.intern();
        validate();
    }

    void validate() throws InvalidANSISequenceException {
        if (!getPattern().matcher(sequenceString).matches()) {
            throw new InvalidANSISequenceException(String.format(
                    "ANSISequence '%s' does not match pattern '%s'.",
                    sequenceString, getPattern().toString()
            ));
        }
    }

    //
    // Getters
    //

    @NotNull
    public abstract Pattern getPattern();

    public byte[] getBytes() {
        return sequenceString.getBytes();
    }
}
