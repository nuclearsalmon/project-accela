package net.accela.prisma.sequence.advanced;

import net.accela.prisma.util.ANSIPatterns;
import net.accela.prisma.util.RegexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single immutable ANSI escape sequence CSI (Control Sequence Introducer).
 */
public class CSISequence extends ANSISequence {
    public enum StatementType {
        CUU, CUD, CUF, CUB, CNL, CPL, CHA, CUP, EID, EIL, SU, SD, HVP, SGR, AP_ON, AP_OFF, DSR,
        P_SCP, P_RCP, P_CUR_ON, P_CUR_OFF, P_ASB_ON, P_ASB_OFF, P_BP_ON, P_BP_OFF,
        UNKNOWN
    }

    CSISequence(@NotNull String sequence) throws InvalidANSISequenceException {
        super(sequence);
    }

    public static @Nullable CSISequence fromString(@NotNull String str) {
        try {
            return new CSISequence(str);
        } catch (InvalidANSISequenceException ignored) {
        }
        return null;
    }

    public final @NotNull CSISequence.StatementType getCSISequenceType() {
        if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CUU)) {
            return StatementType.CUU;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CUD)) {
            return StatementType.CUD;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CUF)) {
            return StatementType.CUF;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CUB)) {
            return StatementType.CUB;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CNL)) {
            return StatementType.CNL;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CPL)) {
            return StatementType.CPL;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CHA)) {
            return StatementType.CHA;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_CUP)) {
            return StatementType.CUP;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_EID)) {
            return StatementType.EID;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_EIL)) {
            return StatementType.EIL;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_SU)) {
            return StatementType.SU;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_SD)) {
            return StatementType.SD;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_HVP)) {
            return StatementType.HVP;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.SGR_sequenceCapture)) {
            return StatementType.SGR;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_AP_ON)) {
            return StatementType.AP_ON;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_AP_OFF)) {
            return StatementType.AP_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_DSR)) {
            return StatementType.DSR;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_SCP)) {
            return StatementType.P_SCP;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_RCP)) {
            return StatementType.P_RCP;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_CUR_ON)) {
            return StatementType.P_CUR_ON;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_CUR_OFF)) {
            return StatementType.P_CUR_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_ASB_ON)) {
            return StatementType.P_ASB_ON;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_ASB_OFF)) {
            return StatementType.P_ASB_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_BP_ON)) {
            return StatementType.P_BP_ON;
        } else if (RegexUtil.testForMatch(sequenceString, ANSIPatterns.CSI_P_BP_OFF)) {
            return StatementType.P_BP_OFF;
        } else {
            return StatementType.UNKNOWN;
        }
    }
}