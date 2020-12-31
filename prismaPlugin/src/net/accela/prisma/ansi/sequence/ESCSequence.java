package net.accela.prisma.ansi.sequence;

import net.accela.prisma.ansi.exception.ESCSequenceException;
import net.accela.prisma.ansi.util.Patterns;
import net.accela.prisma.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Represents a single immutable ANSI escape sequence.
 */
public class ESCSequence implements CharSequence {
    /**
     * Escape key sequence initializer ("\u001b" or "\27")
     */
    @SuppressWarnings("unused")
    public static final String ESC_STRING = "\u001B";

    /**
     * Reset to Initial State
     */
    @SuppressWarnings("unused")
    public static final String RIS_STRING = ESC_STRING + "c";

    /*
     * Reset to Initial State
     */
    /*
    @SuppressWarnings("unused")
    public static final ESCSequence RIS_SEQUENCE = new ESCSequence(ESC_STRING + "c");
    */

    @SuppressWarnings("unused")
    public final static Pattern ESC_SEQUENCE_PATTERN = Patterns.ANSI8Bit;

    @NotNull
    protected String sequenceString = "";

    public ESCSequence() {
    }

    public ESCSequence(@NotNull String sequenceString) throws ESCSequenceException {
        validateString(sequenceString, Patterns.ANSI8Bit);
        this.sequenceString = sequenceString;
    }

    protected static void validateString(@NotNull String string, @NotNull Pattern pattern) {
        if (!pattern.matcher(string).matches()) {
            throw new ESCSequenceException(String.format(
                    "Sequence '%s' does not match pattern '%s'", string, pattern.toString()
            ));
        }
    }

    public enum ESCSequenceType {
        SS2, SS3, DCS, CSI, ST, OSC, SOS, PM, APC, RIS, UNKNOWN
    }

    public final @NotNull ESCSequenceType getESCSequenceType() {
        if (RegexUtil.testForMatch(sequenceString, Patterns.SS2)) {
            return ESCSequenceType.SS2;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.SS3)) {
            return ESCSequenceType.SS3;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.DCS)) {
            return ESCSequenceType.DCS;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI)) {
            return ESCSequenceType.CSI;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.ST)) {
            return ESCSequenceType.ST;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.OSC)) {
            return ESCSequenceType.OSC;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.SOS)) {
            return ESCSequenceType.SOS;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.PM)) {
            return ESCSequenceType.PM;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.APC)) {
            return ESCSequenceType.APC;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.RIS)) {
            return ESCSequenceType.RIS;
        } else return ESCSequenceType.UNKNOWN;
    }

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

    @NotNull
    @Override
    public String toString() {
        return sequenceString;
    }

    @Override
    public IntStream chars() {
        return sequenceString.chars();
    }

    @Override
    public IntStream codePoints() {
        return sequenceString.codePoints();
    }

    @Override
    public boolean equals(Object obj) {
        if (ESCSequence.class.isAssignableFrom(obj.getClass())) {
            ESCSequence escSequence = (ESCSequence) obj;
            return this.toString().equals(escSequence.toString());
        }
        return false;
    }
}
