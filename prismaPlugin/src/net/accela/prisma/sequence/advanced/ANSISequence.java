package net.accela.prisma.sequence.advanced;

import net.accela.prisma.util.ANSIPatterns;
import net.accela.prisma.util.RegexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Represents a single immutable ANSI escape sequence.
 */
public class ANSISequence extends AbstractSequence implements CharSequence {
    public enum ESCSequenceType {
        SS2, SS3, DCS, CSI, ST, OSC, SOS, PM, APC, RIS, UNKNOWN
    }

    //
    // Constructor
    //

    ANSISequence(@NotNull String str) throws InvalidANSISequenceException {
        super(str);
    }

    @Nullable
    public static ANSISequence fromString(@NotNull String str) {
        try {
            return new ANSISequence(str);
        } catch (InvalidANSISequenceException ignored) {
        }
        return null;
    }

    //
    // Miscellaneous
    //

    public @NotNull Pattern getPattern() {
        return ANSIPatterns.ANSI8Bit;
    }

    public byte[] getBytes() {
        return sequenceString.getBytes();
    }

    public final @NotNull ESCSequenceType getESCSequenceType() {
        if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.SS2)) {
            return ESCSequenceType.SS2;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.SS3)) {
            return ESCSequenceType.SS3;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.DCS)) {
            return ESCSequenceType.DCS;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI)) {
            return ESCSequenceType.CSI;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.ST)) {
            return ESCSequenceType.ST;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.OSC)) {
            return ESCSequenceType.OSC;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.SOS)) {
            return ESCSequenceType.SOS;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.PM)) {
            return ESCSequenceType.PM;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.APC)) {
            return ESCSequenceType.APC;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.RIS)) {
            return ESCSequenceType.RIS;
        } else return ESCSequenceType.UNKNOWN;
    }

    //
    // CharSequence overrides
    //

    @Override
    public int length() {
        return sequenceString.length();
    }

    @Override
    public char charAt(int i) {
        return sequenceString.charAt(i);
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        return sequenceString.subSequence(i, i1);
    }

    @Override
    public IntStream chars() {
        return sequenceString.chars();
    }

    @Override
    public IntStream codePoints() {
        return sequenceString.codePoints();
    }

    //
    // Object overrides
    //

    @NotNull
    @Override
    public String toString() {
        return sequenceString;
    }

    @Override
    public boolean equals(Object obj) {
        if (ANSISequence.class.isAssignableFrom(obj.getClass())) {
            ANSISequence ansiSequence = (ANSISequence) obj;
            return this.toString().equals(ansiSequence.toString());
        }
        return false;
    }
}