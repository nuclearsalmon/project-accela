package net.accela.prisma.ansi.sequence;

import net.accela.prisma.ansi.exception.ESCSequenceException;
import net.accela.prisma.ansi.util.Patterns;
import net.accela.prisma.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Represents a single immutable ANSI escape sequence CSI (Control Sequence Introducer).
 */
public class CSISequence extends ESCSequence {
    /**
     * A pattern that matches all valid CSISequences
     */
    @SuppressWarnings("unused")
    public static final Pattern CSI_SEQUENCE_PATTERN = Patterns.CSI;

    /**
     * CSI - Control Sequence Inducer
     */
    @SuppressWarnings("unused")
    public static final String CSI_STRING = ESC_STRING + "[";

    public static final CSISequence P_CUR_ON = new CSISequence(CSI_STRING + "?25h");
    public static final CSISequence P_CUR_OFF = new CSISequence(CSI_STRING + "?25l");

    /**
     * Clears the terminal window.
     */
    public static String CLR_STRING = CSISequence.CSI_STRING + "2J";

    public enum StatementType {
        CUU, CUD, CUF, CUB, CNL, CPL, CHA, CUP, EID, EIL, SU, SD, HVP, SGR, AP_ON, AP_OFF, DSR,
        P_SCP, P_RCP, P_CUR_ON, P_CUR_OFF, P_ASB_ON, P_ASB_OFF, P_BP_ON, P_BP_OFF,
        UNKNOWN
    }

    public CSISequence() {
    }

    public CSISequence(@NotNull String sequence) throws ESCSequenceException {
        validateString(sequence, Patterns.CSI);
        this.sequenceString = sequence;
    }

    public final @NotNull CSISequence.StatementType getCSISequenceType() {
        if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUU)) {
            return StatementType.CUU;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUD)) {
            return StatementType.CUD;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUF)) {
            return StatementType.CUF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUB)) {
            return StatementType.CUB;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CNL)) {
            return StatementType.CNL;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CPL)) {
            return StatementType.CPL;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CHA)) {
            return StatementType.CHA;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUP)) {
            return StatementType.CUP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_EID)) {
            return StatementType.EID;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_EIL)) {
            return StatementType.EIL;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_SU)) {
            return StatementType.SU;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_SD)) {
            return StatementType.SD;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_HVP)) {
            return StatementType.HVP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.SGR)) {
            return StatementType.SGR;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_AP_ON)) {
            return StatementType.AP_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_AP_OFF)) {
            return StatementType.AP_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_DSR)) {
            return StatementType.DSR;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_SCP)) {
            return StatementType.P_SCP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_RCP)) {
            return StatementType.P_RCP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_CUR_ON)) {
            return StatementType.P_CUR_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_CUR_OFF)) {
            return StatementType.P_CUR_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_ASB_ON)) {
            return StatementType.P_ASB_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_ASB_OFF)) {
            return StatementType.P_ASB_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_BP_ON)) {
            return StatementType.P_BP_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_BP_OFF)) {
            return StatementType.P_BP_OFF;
        } else {
            return StatementType.UNKNOWN;
        }
    }
}
