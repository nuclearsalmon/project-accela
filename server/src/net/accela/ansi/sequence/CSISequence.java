package net.accela.ansi.sequence;

import net.accela.ansi.Patterns;
import net.accela.ansi.exception.ESCSequenceException;
import net.accela.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * A class representing a single ANSI CSI (Control Sequence Introducer) sequence.
 * This abstraction was implemented both as a security measure, and as a convenience tool.
 */
public class CSISequence extends ESCSequence {
    public final static Pattern sequencePattern = Patterns.CSI;

    public CSISequence() {
    }

    public CSISequence(@NotNull String sequence) throws ESCSequenceException {
        validate(sequence, Patterns.CSI);
        this.sequenceString = sequence;
    }

    public enum CSISequenceType {
        CUU, CUD, CUF, CUB, CNL, CPL, CHA, CUP, ED, EL, SU, SD, HVP, SGR, AP_ON, AP_OFF, DSR,
        P_SCP, P_RCP, P_CUR_ON, P_CUR_OFF, P_ASB_ON, P_ASB_OFF, P_BP_ON, P_BP_OFF,
        UNKNOWN
    }

    public final @NotNull CSISequenceType getCSISequenceType() {
        if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUU)) {
            return CSISequenceType.CUU;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUD)) {
            return CSISequenceType.CUD;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUF)) {
            return CSISequenceType.CUF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUB)) {
            return CSISequenceType.CUB;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CNL)) {
            return CSISequenceType.CNL;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CPL)) {
            return CSISequenceType.CPL;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CHA)) {
            return CSISequenceType.CHA;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_CUP)) {
            return CSISequenceType.CUP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_ED)) {
            return CSISequenceType.ED;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_EL)) {
            return CSISequenceType.EL;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_SU)) {
            return CSISequenceType.SU;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_SD)) {
            return CSISequenceType.SD;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_HVP)) {
            return CSISequenceType.HVP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.SGR)) {
            return CSISequenceType.SGR;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_AP_ON)) {
            return CSISequenceType.AP_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_AP_OFF)) {
            return CSISequenceType.AP_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_DSR)) {
            return CSISequenceType.DSR;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_SCP)) {
            return CSISequenceType.P_SCP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_RCP)) {
            return CSISequenceType.P_RCP;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_CUR_ON)) {
            return CSISequenceType.P_CUR_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_CUR_OFF)) {
            return CSISequenceType.P_CUR_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_ASB_ON)) {
            return CSISequenceType.P_ASB_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_ASB_OFF)) {
            return CSISequenceType.P_ASB_OFF;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_BP_ON)) {
            return CSISequenceType.P_BP_ON;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI_P_BP_OFF)) {
            return CSISequenceType.P_BP_OFF;
        } else {
            return CSISequenceType.UNKNOWN;
        }
    }
}
