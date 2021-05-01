package net.accela.prisma.util;

import java.util.regex.Pattern;

/**
 * A collection of various regex patterns used for matching various ANSI control sequences.
 * Don't write any methods in this class, just regex patterns. Refer to {@link ANSIUtils} for that.
 */
@SuppressWarnings("unused")
public class ANSIPatterns {
    /**
     * Matches 7-bit control sequences
     */
    @Deprecated
    public static final Pattern ANSI7Bit = Pattern.compile(
            // ESC
            "\u001B"
                    // 7-bit C1, two bytes and ESC Fe (omitting CSI) or [
                    + "(?:[@-Z\\-_]|\\["
                    // Parameter bytes
                    + "[0-?]*"
                    // Intermediate bytes
                    + "[ -/]*"
                    // Final byte
                    + "[@-~]"
                    + ")"
    );

    /**
     * Matches and collects 8-bit control sequences
     */
    public static final Pattern ANSI8Bit = Pattern.compile(
            "(\\x1B[@-Z\\\\-_]|[\\x80-\\x9A\\x9C-\\x9F]|(?:\\x1B\\[|\\x9B)[0-?]*[ -/]*[@-~])"
    );

    /**
     * Matches 8-bit control sequences
     */
    @Deprecated
    public static final Pattern ANSI8bit_old = Pattern.compile(
            // Either 7-bit C1, two bytes and ESC Fe (omitting CSI)
            "(?:\u001B[@-Z\\-_]"
                    // or a single 8-bit byte Fe (omitting CSI)
                    + "|[\u0080-\u009A\u009C-\u009F]"
                    // or CSI + control codes
                    + "|(?:"
                    // 7-bit CSI, ESC[
                    + "\u001B\\["
                    // or 8-bit CSI, 9B
                    + "|\u009B)"
                    // Parameter bytes
                    + "[0-?]*"
                    // Intermediate bytes
                    + "[ -/]*"
                    // Final byte
                    + "[@-~]"
                    + ")"
    );

    /**
     * Matches SS2 (Single Shift Two)
     */
    public static final Pattern SS2 = Pattern.compile("\u001BN");

    /**
     * Matches SS3 (Single Shift Three)
     */
    public static final Pattern SS3 = Pattern.compile("\u001BO");

    /**
     * Matches DCS (Device Control String)
     */
    public static final Pattern DCS = Pattern.compile("\u001BP");

    /**
     * Matches ST (String Terminator)
     */
    public static final Pattern ST = Pattern.compile("\u001B\\\\");

    /**
     * Matches OSC (Operating System Command)
     */
    public static final Pattern OSC = Pattern.compile("\u001B]");

    /**
     * Matches SOS (Start of String)
     */
    public static final Pattern SOS = Pattern.compile("\u001BX");

    /**
     * Matches PM (Privacy Message)
     */
    public static final Pattern PM = Pattern.compile("\u001B\\^");

    /**
     * Matches APC (Application Program Command)
     */
    public static final Pattern APC = Pattern.compile("\u001B_");

    /**
     * Matches RIS (Reset to Initial State)
     */
    public static final Pattern RIS = Pattern.compile("\u001Bc");

    /**
     * Matches CSI (Control ANSISequence Introducer)
     */
    public static final Pattern CSI = Pattern.compile("\u001B\\[[^\u001B]+");

    /**
     * Matches CSI_CUU (Cursor Up)
     */
    public static final Pattern CSI_CUU = Pattern.compile("\u001B\\[[0-9]+A");

    /**
     * Matches CSI_CUD (Cursor Down)
     */
    public static final Pattern CSI_CUD = Pattern.compile("\u001B\\[[0-9]+B");

    /**
     * Matches CSI_CUF (Cursor Forward)
     */
    public static final Pattern CSI_CUF = Pattern.compile("\u001B\\[[0-9]+C");

    /**
     * Matches CSI_CUB (Cursor Back)
     */
    public static final Pattern CSI_CUB = Pattern.compile("\u001B\\[[0-9]+D");

    /**
     * Matches CSI_CNL (Cursor Next Line)
     */
    public static final Pattern CSI_CNL = Pattern.compile("\u001B\\[[0-9]+E");

    /**
     * Matches CSI_CPL (Cursor Previous Line)
     */
    public static final Pattern CSI_CPL = Pattern.compile("\u001B\\[[0-9]+F");

    /**
     * Matches CSI_CHA (Cursor Horizontal Absolute)
     */
    public static final Pattern CSI_CHA = Pattern.compile("\u001B\\[[0-9]+G");

    /**
     * Matches CSI_CUP (Cursor Position)
     */
    public static final Pattern CSI_CUP = Pattern.compile("\u001B\\[[0-9]+;[0-9]+H");

    /**
     * Matches CSI_EID (Erase in Display)
     */
    public static final Pattern CSI_EID = Pattern.compile("\u001B\\[[0-9]+J");

    /**
     * Matches CSI_EIL (Erase in Line)
     */
    public static final Pattern CSI_EIL = Pattern.compile("\u001B\\[[0-9]+K");

    /**
     * Matches CSI_SU (Scroll Up)
     */
    public static final Pattern CSI_SU = Pattern.compile("\u001B\\[[0-9]+S");

    /**
     * Matches CSI_SD (Scroll Down)
     */
    public static final Pattern CSI_SD = Pattern.compile("\u001B\\[[0-9]+T");

    /**
     * Matches CSI_HVP (Horizontal Vertical Position)
     */
    public static final Pattern CSI_HVP = Pattern.compile("\u001B\\[[0-9]+;[0-9]+f");

    /**
     * Matches CSI_APN (Enable AUX serial Port)
     */
    public static final Pattern CSI_AP_ON = Pattern.compile("\u001B\\[5i");

    /**
     * Matches CSI_APF (Disable AUX serial Port)
     */
    public static final Pattern CSI_AP_OFF = Pattern.compile("\u001B\\[4i");

    /**
     * Matches vDSR (Device Status Report)
     */
    public static final Pattern CSI_DSR = Pattern.compile("\u001B\\[6n");


    //
    // - (Private) CSI patterns -
    //

    /**
     * Matches CSI_P_SCP/SCOSC (Save Cursor Position)
     */
    public static final Pattern CSI_P_SCP = Pattern.compile("\u001B\\[s");

    /**
     * Matches CSI_P_RCP/SCORC (Restore Cursor Position)
     */
    public static final Pattern CSI_P_RCP = Pattern.compile("\u001B\\[u");

    /**
     * Matches CSI_P_CUR_ON (Show Cursor)
     */
    public static final Pattern CSI_P_CUR_ON = Pattern.compile("\u001B\\[?25h");

    /**
     * Matches CSI_P_CUR_OFF (Hide Cursor)
     */
    public static final Pattern CSI_P_CUR_OFF = Pattern.compile("\u001B\\[?25l");

    /**
     * Matches CSI_P_ASB_ON (Enable Alternative Screen Buffer)
     */
    public static final Pattern CSI_P_ASB_ON = Pattern.compile("\u001B\\[?1049h");

    /**
     * Matches CSI_P_ASB_OFF (Disable Alternative Screen Buffer)
     */
    public static final Pattern CSI_P_ASB_OFF = Pattern.compile("\u001B\\[?1049l");

    /**
     * Matches CSI_P_BP_ON (Enable Bracketed Paste Mode)
     */
    public static final Pattern CSI_P_BP_ON = Pattern.compile("\u001B\\[2004h");

    /**
     * Matches CSI_P_BP_OFF (Disable Bracketed Paste Mode)
     */
    public static final Pattern CSI_P_BP_OFF = Pattern.compile("\u001B\\[2004l");


    //
    // - SGR patterns -
    //

    /**
     * Matches SGR (Select Graphic Rendition)
     */
    public static final Pattern SGR_sequenceCapture = Pattern.compile(
            "([\\x1B\\x9B]\\[(?:[0-9]{1,4}(?:;[0-9]{0,4})*)?[m])"
    );

    /**
     *
     */
    public static final Pattern SGR_statementCapture = Pattern.compile(
            "([\\x1B\\x9B]\\[(?:[0-9]{1,4}(?:;[0-9]{0,4})*)?[m])"
    );

    /**
     * Matches SGR_RESET (Reset / Normal)
     */
    public static final Pattern SGR_RESET = Pattern.compile("\u001B\\[0m");

    /**
     * Matches SGR_BOLD_ON (Bold / Increased intensity)
     */
    public static final Pattern SGR_BOLD_ON = Pattern.compile("\u001B\\[1m");

    /**
     * Matches SGR_FAINT_ON (Faint / Decreased intensity)
     */
    public static final Pattern SGR_FAINT_ON = Pattern.compile("\u001B\\[2m");

    /**
     * Matches SGR_ITALIC_ON (Italic)
     */
    public static final Pattern SGR_ITALIC_ON = Pattern.compile("\u001B\\[3m");

    /**
     * Matches SGR_UNDERLINE_ON (Underline)
     */
    public static final Pattern SGR_UNDERLINE_ON = Pattern.compile("\u001B\\[4m");

    /**
     * Matches SGR_SLOW_BLINK_ON (Slow Blink / Blink less than 150 per minute)
     */
    public static final Pattern SGR_SLOW_BLINK_ON = Pattern.compile("\u001B\\[5m");

    /**
     * Matches SGR_RAPID_BLINK_ON (Rapid Blink / Blink more than 150 per minute)
     */
    public static final Pattern SGR_RAPID_BLINK_ON = Pattern.compile("\u001B\\[6m");

    /**
     * Matches SGR_REVERSE_ON (Reverse / Inverse video on)
     */
    public static final Pattern SGR_REVERSE_ON = Pattern.compile("\u001B\\[7m");

    /**
     * Matches SGR_CONCEAL_ON (Conceal / Hide)
     */
    public static final Pattern SGR_CONCEAL_ON = Pattern.compile("\u001B\\[8m");

    /**
     * Matches SGR_STRIKE_ON (Crossed out / Strike)
     */
    public static final Pattern SGR_STRIKE_ON = Pattern.compile("\u001B\\[9m");

    /**
     * Matches SGR_FONT_DEF (Primary Font / Default Font)
     */
    public static final Pattern SGR_FONT_DEF = Pattern.compile("\u001B\\[10m");

    /**
     * Matches SGR_FONT_ALT (Alternative Font)
     */
    public static final Pattern SGR_FONT_ALT = Pattern.compile("\u001B\\[1[1-9]m");

    /**
     * Matches SGR_FRAKTUR_ON (Fraktur)
     */
    public static final Pattern SGR_FRAKTUR_ON = Pattern.compile("\u001B\\[20m");

    /**
     * Matches SGR_DOUBLE_UNDERLINE_OFF (Double Underline OR Bold Off)
     */
    public static final Pattern SGR_DOUBLE_UNDERLINE_OFF = Pattern.compile("\u001B\\[21m");

    /**
     * Matches SGR_NORMAL (Normal Color / Normal Intensity)
     */
    public static final Pattern SGR_NORMAL = Pattern.compile("\u001B\\[22m");

    /**
     * Matches SGR_ITALIC_FRAKTUR_OFF (Italic and Fraktur off)
     */
    public static final Pattern SGR_ITALIC_FRAKTUR_OFF = Pattern.compile("\u001B\\[23m");

    /**
     * Matches SGR_UNDERLINE_OFF (Underline off)
     */
    public static final Pattern SGR_UNDERLINE_OFF = Pattern.compile("\u001B\\[24m");

    /**
     * Matches SGR_BLINK_OFF (Blink off)
     */
    public static final Pattern SGR_BLINK_OFF = Pattern.compile("\u001B\\[25m");

    /**
     * Matches SGR_PROP_SPACING_ON (Proportional Spacing)
     */
    public static final Pattern SGR_PROP_SPACING_ON = Pattern.compile("\u001B\\[26m");

    /**
     * Matches SGR_REVERSE_OFF (Reverse / Invert video off)
     */
    public static final Pattern SGR_REVERSE_OFF = Pattern.compile("\u001B\\[27m");

    /**
     * Matches SGR_CONCEAL_OFF (Conceal off / Reveal)
     */
    public static final Pattern SGR_CONCEAL_OFF = Pattern.compile("\u001B\\[28m");

    /**
     * Matches SGR_STRIKE_OFF (Strike off / (Not (crossed out / strikethrough)))
     */
    public static final Pattern SGR_STRIKE_OFF = Pattern.compile("\u001B\\[29m");

    /**
     * Matches SGR_FG_4B (Set foreground color (3/4-bit))
     */
    public static final Pattern SGR_FG_4B = Pattern.compile("\u001B\\[3[0-7]m");

    /**
     * Matches SGR_FG_8B (Set foreground color (8-bit))
     */
    public static final Pattern SGR_FG_8B = Pattern.compile("\u001B\\[38;5;[0-9]+m");

    /**
     * Matches SGR_FG_24B (Set foreground color (24-bit))
     */
    public static final Pattern SGR_FG_24B = Pattern.compile("\u001B\\[38;2;[0-9]+;[0-9]+;[0-9]+m");

    /**
     * Matches SGR_FG_DEF (Set default foreground color)
     */
    public static final Pattern SGR_FG_DEF = Pattern.compile("\u001B\\[39m");

    /**
     * Matches SGR_BG_4B (Set background color (3/4-bit))
     */
    public static final Pattern SGR_BG_4B = Pattern.compile("\u001B\\[4[0-7]m");

    /**
     * Matches SGR_BG_8B (Set background color (8-bit))
     */
    public static final Pattern SGR_BG_8B = Pattern.compile("\u001B\\[48;5;[0-9]+m");

    /**
     * Matches SGR_BG_24B (Set background color (24-bit))
     */
    public static final Pattern SGR_BG_24B = Pattern.compile("\u001B\\[48;2;[0-9]+;[0-9]+;[0-9]+m");

    /**
     * Matches SGR_BG_DEF (Set default background color)
     */
    public static final Pattern SGR_BG_DEF = Pattern.compile("\u001B\\[49m");

    /**
     * Matches SGR_PROP_SPACING_OFF (Disable Proportional Spacing)
     */
    public static final Pattern SGR_PROP_SPACING_OFF = Pattern.compile("\u001B\\[50m");

    /**
     * Matches SGR_FRAMED_ON (Enable Framed)
     */
    public static final Pattern SGR_FRAMED_ON = Pattern.compile("\u001B\\[51m");

    /**
     * Matches SGR_ENCIRCLED_ON (Enable Encircled)
     */
    public static final Pattern SGR_ENCIRCLED_ON = Pattern.compile("\u001B\\[52m");

    /**
     * Matches SGR_OVERLINED_ON (Enable Overlined)
     */
    public static final Pattern SGR_OVERLINED_ON = Pattern.compile("\u001B\\[53m");

    /**
     * Matches SGR_FRAMED_ENCIRCLED_OFF (Disable Framed and Encircled)
     */
    public static final Pattern SGR_FRAMED_ENCIRCLED_OFF = Pattern.compile("\u001B\\[54m");

    /**
     * Matches SGR_OVERLINED_OFF (Disable Overlined)
     */
    public static final Pattern SGR_OVERLINED_OFF = Pattern.compile("\u001B\\[55m");

    /**
     * Matches SGR_UNDERLINE_COLOR (Set Underline color)
     */
    public static final Pattern SGR_UNDERLINE_COLOR = Pattern.compile("\u001B\\[58m");

    /**
     * Matches SGR_UNDERLINE_COLOR_DEF (Reset / Normal Underline color)
     */
    public static final Pattern SGR_UNDERLINE_COLOR_DEF = Pattern.compile("\u001B\\[59m");

    /**
     * Matches SGR_IDEOGRAM_UNDERLINE_ON (Ideogram underline or right side line on)
     */
    public static final Pattern SGR_IDEOGRAM_UNDERLINE_ON = Pattern.compile("\u001B\\[60m");

    /**
     * Matches SGR_IDEOGRAM_DOUBLE_UNDERLINE_ON (Ideogram double underline or double line on the right side on)
     */
    public static final Pattern SGR_IDEOGRAM_DOUBLE_UNDERLINE_ON = Pattern.compile("\u001B\\[61m");

    /**
     * Matches SGR_IDEOGRAM_OVERLINE_ON (Ideogram overline or left side line on)
     */
    public static final Pattern SGR_IDEOGRAM_OVERLINE_ON = Pattern.compile("\u001B\\[62m");

    /**
     * Matches SGR_IDEOGRAM_DOUBLE_OVERLINE_ON (Ideogram double overline or double line on the left side on)
     */
    public static final Pattern SGR_IDEOGRAM_DOUBLE_OVERLINE_ON = Pattern.compile("\u001B\\[63m");

    /**
     * Matches SGR_IDEOGRAM_STRESS_MARKING_ON (Ideogram stress marking on)
     */
    public static final Pattern SGR_IDEOGRAM_STRESS_MARKING_ON = Pattern.compile("\u001B\\[64m");

    /**
     * Matches SGR_IDEOGRAM_OFF (Ideogram attributes off)
     */
    public static final Pattern SGR_IDEOGRAM_OFF = Pattern.compile("\u001B\\[65m");

    /**
     * Matches SGR_SUPERSCRIPT_ON (Superscript on (Mintty))
     */
    public static final Pattern SGR_SUPERSCRIPT_ON = Pattern.compile("\u001B\\[73m");

    /**
     * Matches SGR_SUBSCRIPT_ON (Subscript on (Mintty))
     */
    public static final Pattern SGR_SUBSCRIPT_ON = Pattern.compile("\u001B\\[74m");

    /**
     * Matches SGR_FG_BRIGHT (Set bright foreground color (Aixterm))
     */
    public static final Pattern SGR_FG_BRIGHT = Pattern.compile("\u001B\\[9[0-7]m");

    /**
     * Matches SGR_BG_BRIGHT (Set bright background color (Aixterm))
     */
    public static final Pattern SGR_BG_BRIGHT = Pattern.compile("\u001B\\[10[0-7]m");
}
