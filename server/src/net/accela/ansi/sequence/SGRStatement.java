package net.accela.ansi.sequence;

import net.accela.ansi.annotation.Inconsistent;
import net.accela.ansi.annotation.NonStandard;
import net.accela.ansi.annotation.NotWidelySupported;
import net.accela.ansi.exception.ESCSequenceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;

public class SGRStatement {
    @NotNull
    final SGRStatement.Type type;
    @Nullable int[] arguments;

    public SGRStatement(int typeAsInt) throws ESCSequenceException {
        this(typeAsInt, null);
    }

    public SGRStatement(int typeAsInt, @Nullable int[] args) throws ESCSequenceException {
        Type typeObj = intToTypeMap.get(typeAsInt);
        if (typeObj == null) throw new ESCSequenceException("Invalid/Unknown SGRStatement type '" + typeAsInt + "'");

        this.type = typeObj;
        this.arguments = args;

        validateArgs(getTypeAsInt(), args);
    }

    public SGRStatement(@NotNull SGRStatement.Type type) throws ESCSequenceException {
        this(type, null);
    }

    public SGRStatement(@NotNull SGRStatement.Type type, @Nullable int[] args) throws ESCSequenceException {
        this.type = type;
        this.arguments = args;

        validateArgs(getTypeAsInt(), args);
    }

    @NotNull
    public SGRStatement.Type getType() {
        return type;
    }

    @Nullable
    public int[] getArguments() {
        return arguments;
    }

    @NotNull
    public SGRSequence toSGRSequence() throws ESCSequenceException {
        return new SGRSequence(this);
    }

    public int getTypeAsInt() {
        return typeToIntMap.get(type);
    }

    public @NotNull String getTypeAsString() {
        return Integer.toString(getTypeAsInt());
    }

    public int[] toIntArray() {
        final int[] statementAsIntArray = new int[1 + arguments.length];
        statementAsIntArray[0] = getTypeAsInt();

        int index = 1;
        for (int arg : arguments) {
            statementAsIntArray[index] = arg;
        }

        return statementAsIntArray;
    }

    @Override
    public String toString() {
        return Arrays.toString(toIntArray());
    }

    static void validateArgs(int type, int[] args) throws ESCSequenceException {
        // Prep some common strings since they're the same across all exceptions.
        final String typeExStr = "SGR type '" + type + "' ('" + intToTypeMap.get(type) + "')";
        final String argsExStr = "Provided arguments: '" + Arrays.toString(args) + "'";

        if (type == 38 | type == 48 | type == 58) {
            if (args == null) throw new ESCSequenceException(typeExStr + " requires arguments. " + argsExStr);

            try {
                int validArgumentsLength;
                if (args[0] == 5) validArgumentsLength = 1;
                else if (args[0] == 2) validArgumentsLength = 3;
                else throw new ESCSequenceException("Unknown colour type '" + args[0] + "' for " + typeExStr);

                for (int i = 1; i <= validArgumentsLength; i++) {
                    int rgbArg = args[i];
                    if (rgbArg < 0 | rgbArg > 255)
                        throw new ESCSequenceException("Invalid RGB value: '" + rgbArg + "'");
                }
            } catch (IndexOutOfBoundsException ignored) {
                throw new ESCSequenceException("Too few arguments for " + typeExStr + ". " + argsExStr);
            }
        } else if (args != null)
            throw new ESCSequenceException(typeExStr + " does not support arguments. " + argsExStr);
    }

    public enum Type {
        // Reset
        /**
         * Resets all attributes
         */
        RESET,

        // Intensity
        /**
         * Enables Bright color intensity OR bold typefaces (this can vary between terminals).
         *
         * @see Type#INTENSITY_DIM_OR_THIN
         * @see Type#INTENSITY_DEFAULT
         */
        @Inconsistent INTENSITY_BRIGHT_OR_BOLD,

        /**
         * Enables dim color intensity OR faint typefaces (this can vary between terminals).
         *
         * @see Type#INTENSITY_BRIGHT_OR_BOLD
         * @see Type#INTENSITY_DEFAULT
         */
        @Inconsistent INTENSITY_DIM_OR_THIN,

        /**
         * Resets color intensity back to normal
         *
         * @see Type#INTENSITY_BRIGHT_OR_BOLD
         * @see Type#INTENSITY_DIM_OR_THIN
         */
        INTENSITY_DEFAULT,

        // Styles
        /**
         * Enables italic style. Not widely supported. Sometimes treated as an inverse or blink effect.
         *
         * @see Type#STYLE_FRAKTUR
         * @see Type#STYLE_DEFAULT
         */
        @NotWidelySupported @Inconsistent STYLE_ITALIC,

        /**
         * Enables fraktur style. Rarely supported.
         *
         * @see Type#STYLE_ITALIC
         * @see Type#STYLE_DEFAULT
         */
        @NotWidelySupported STYLE_FRAKTUR,

        /**
         * Resets style back to normal. Disables italic and fraktur styles
         *
         * @see Type#STYLE_ITALIC
         * @see Type#STYLE_FRAKTUR
         */
        STYLE_DEFAULT,

        // Cursor blinking
        /**
         * Enables slow cursor blinking. Less than 150 blinks per minute, according to the spec.
         *
         * @see Type#BLINK_FAST
         * @see Type#BLINK_DEFAULT
         */
        BLINK_SLOW,

        /**
         * Enables fast cursor blinking. More than 150 blinks per minute, according to the spec.
         *
         * @see Type#BLINK_SLOW
         * @see Type#BLINK_DEFAULT
         */
        @NotWidelySupported BLINK_FAST,

        /**
         * Disables cursor blinking.
         *
         * @see Type#BLINK_SLOW
         * @see Type#BLINK_FAST
         */
        BLINK_DEFAULT,

        // Inversion
        /**
         * Enables color inversion by swapping the foreground and background colors.
         * Also known as "reverse video". Inconsistent emulation.
         *
         * @see Type#INVERT_OFF
         */
        @Inconsistent INVERT_ON,

        /**
         * Disables the color inversion effect; disables swapping foreground and background colors.
         *
         * @see Type#INVERT_ON
         */
        INVERT_OFF,

        // Concealment
        /**
         * Enables text concealing.
         * Also known as "Hide". Not widely supported.
         *
         * @see Type#CONCEAL_OFF
         */
        @NotWidelySupported CONCEAL_ON,

        /**
         * Disable text concealing.
         *
         * @see Type#CONCEAL_ON
         */
        CONCEAL_OFF,

        // Strikethrough
        /**
         * Enables a strike-through line across text. Also known as "Crossed out".
         * Makes characters legible, but marked as if for deletion.
         *
         * @see Type#STRIKE_OFF
         */
        @NotWidelySupported STRIKE_ON,

        /**
         * Disables strike-through lines across text.
         *
         * @see Type#STRIKE_ON
         */
        STRIKE_OFF,

        // Fonts
        // todo Finish documenting this
        @Inconsistent FONT_1, @Inconsistent FONT_2, @Inconsistent FONT_3,
        @Inconsistent FONT_4, @Inconsistent FONT_5, @Inconsistent FONT_6,
        @Inconsistent FONT_7, @Inconsistent FONT_8, @Inconsistent FONT_9,
        FONT_DEFAULT,

        // Underlines
        UNDERLINE_SINGLE,
        UNDERLINE_DOUBLE,
        UNDERLINE_NONE,

        // Color
        FG_BLK, FG_RED, FG_GRN, FG_YEL, FG_BLU, FG_MAG, FG_CYA, FG_WHI, FG_RGB,
        FG_DEFAULT,

        BG_BLK, BG_RED, BG_GRN, BG_YEL, BG_BLU, BG_MAG, BG_CYA, BG_WHI, BG_RGB,
        BG_DEFAULT,

        // Bright color
        @NonStandard FG_BLK_BRIGHT, @NonStandard FG_RED_BRIGHT, @NonStandard FG_GRN_BRIGHT, @NonStandard FG_YEL_BRIGHT,
        @NonStandard FG_BLU_BRIGHT, @NonStandard FG_MAG_BRIGHT, @NonStandard FG_CYA_BRIGHT, @NonStandard FG_WHI_BRIGHT,

        @NonStandard BG_BLK_BRIGHT, @NonStandard BG_RED_BRIGHT, @NonStandard BG_GRN_BRIGHT, @NonStandard BG_YEL_BRIGHT,
        @NonStandard BG_BLU_BRIGHT, @NonStandard BG_MAG_BRIGHT, @NonStandard BG_CYA_BRIGHT, @NonStandard BG_WHI_BRIGHT,

        // Underline Color
        @NonStandard UNDERLINE_COLOR,
        @NonStandard UNDERLINE_COLOR_DEFAULT,

        // Proportional spacing
        @NotWidelySupported PROP_SPACING_ON, @NotWidelySupported PROP_SPACING_OFF,

        // Framing
        @NotWidelySupported FRAMED_ON, @NotWidelySupported ENCIRCLED_ON, @NotWidelySupported OVERLINED_ON,
        @NotWidelySupported FRAMED_ENCIRCLED_OFF, @NotWidelySupported OVERLINED_OFF,

        // Ideogram
        @NotWidelySupported IDEOGRAM_UNDERLINE_ON, @NotWidelySupported IDEOGRAM_DOUBLE_UNDERLINE_ON,
        @NotWidelySupported IDEOGRAM_OVERLINE_ON, @NotWidelySupported IDEOGRAM_DOUBLE_OVERLINE_ON,
        @NotWidelySupported IDEOGRAM_STRESS_MARKING_ON, @NotWidelySupported IDEOGRAM_OFF,

        // Subscript and superscript
        /**
         * Enables subscript. Not widely supported, not in standard. Supported in mintty.
         */
        @NotWidelySupported @NonStandard SUBSCRIPT,

        /**
         * Enables superscript. Not widely supported, not in standard. Supported in mintty.
         */
        @NotWidelySupported @NonStandard SUPERSCRIPT,
    }

    @SuppressWarnings("unused")
    public static final HashMap<Integer, Type> intToTypeMap = new HashMap<>() {{
        put(0, Type.RESET);
        put(1, Type.INTENSITY_BRIGHT_OR_BOLD);
        put(2, Type.INTENSITY_DIM_OR_THIN);
        put(3, Type.STYLE_ITALIC);
        put(4, Type.UNDERLINE_SINGLE);
        put(5, Type.BLINK_SLOW);
        put(6, Type.BLINK_FAST);
        put(7, Type.INVERT_ON);
        put(8, Type.CONCEAL_ON);
        put(9, Type.STRIKE_ON);
        put(10, Type.FONT_DEFAULT);
        put(11, Type.FONT_1);
        put(12, Type.FONT_2);
        put(13, Type.FONT_3);
        put(14, Type.FONT_4);
        put(15, Type.FONT_5);
        put(16, Type.FONT_6);
        put(17, Type.FONT_7);
        put(18, Type.FONT_8);
        put(19, Type.FONT_9);
        put(20, Type.STYLE_FRAKTUR);
        put(21, Type.UNDERLINE_DOUBLE);
        put(22, Type.INTENSITY_DEFAULT);
        put(23, Type.STYLE_DEFAULT);
        put(24, Type.UNDERLINE_NONE);
        put(25, Type.BLINK_DEFAULT);
        put(26, Type.PROP_SPACING_ON);
        put(27, Type.INVERT_OFF);
        put(28, Type.CONCEAL_OFF);
        put(29, Type.STRIKE_OFF);
        put(30, Type.FG_BLK);
        put(31, Type.FG_RED);
        put(32, Type.FG_GRN);
        put(33, Type.FG_YEL);
        put(34, Type.FG_BLU);
        put(35, Type.FG_MAG);
        put(36, Type.FG_CYA);
        put(37, Type.FG_WHI);
        put(38, Type.FG_RGB);
        put(39, Type.FG_DEFAULT);
        put(40, Type.BG_BLK);
        put(41, Type.BG_RED);
        put(42, Type.BG_GRN);
        put(43, Type.BG_YEL);
        put(44, Type.BG_BLU);
        put(45, Type.BG_MAG);
        put(46, Type.BG_CYA);
        put(47, Type.BG_WHI);
        put(48, Type.BG_RGB);
        put(49, Type.BG_DEFAULT);
        put(50, Type.PROP_SPACING_OFF);
        put(51, Type.FRAMED_ON);
        put(52, Type.ENCIRCLED_ON);
        put(53, Type.OVERLINED_ON);
        put(54, Type.FRAMED_ENCIRCLED_OFF);
        put(55, Type.OVERLINED_OFF);
        put(56, Type.UNDERLINE_COLOR);
        put(57, Type.UNDERLINE_COLOR_DEFAULT);
        put(60, Type.IDEOGRAM_UNDERLINE_ON);
        put(61, Type.IDEOGRAM_DOUBLE_UNDERLINE_ON);
        put(62, Type.IDEOGRAM_OVERLINE_ON);
        put(63, Type.IDEOGRAM_DOUBLE_OVERLINE_ON);
        put(64, Type.IDEOGRAM_STRESS_MARKING_ON);
        put(65, Type.IDEOGRAM_OFF);
        put(73, Type.SUPERSCRIPT);
        put(74, Type.SUBSCRIPT);
        put(90, Type.FG_BLK_BRIGHT);
        put(91, Type.FG_RED_BRIGHT);
        put(92, Type.FG_GRN_BRIGHT);
        put(93, Type.FG_YEL_BRIGHT);
        put(94, Type.FG_BLU_BRIGHT);
        put(95, Type.FG_MAG_BRIGHT);
        put(96, Type.FG_CYA_BRIGHT);
        put(97, Type.FG_WHI_BRIGHT);
        put(100, Type.BG_BLK_BRIGHT);
        put(101, Type.BG_RED_BRIGHT);
        put(102, Type.BG_GRN_BRIGHT);
        put(103, Type.BG_YEL_BRIGHT);
        put(104, Type.BG_BLU_BRIGHT);
        put(105, Type.BG_MAG_BRIGHT);
        put(106, Type.BG_CYA_BRIGHT);
        put(107, Type.BG_WHI_BRIGHT);
    }};

    @SuppressWarnings("unused")
    public static final HashMap<Type, Integer> typeToIntMap = new HashMap<>() {{
        put(Type.RESET, 0);
        put(Type.INTENSITY_BRIGHT_OR_BOLD, 1);
        put(Type.INTENSITY_DIM_OR_THIN, 2);
        put(Type.STYLE_ITALIC, 3);
        put(Type.UNDERLINE_SINGLE, 4);
        put(Type.BLINK_SLOW, 5);
        put(Type.BLINK_FAST, 6);
        put(Type.INVERT_ON, 7);
        put(Type.CONCEAL_ON, 8);
        put(Type.STRIKE_ON, 9);
        put(Type.FONT_DEFAULT, 10);
        put(Type.FONT_1, 11);
        put(Type.FONT_2, 12);
        put(Type.FONT_3, 13);
        put(Type.FONT_4, 14);
        put(Type.FONT_5, 15);
        put(Type.FONT_6, 16);
        put(Type.FONT_7, 17);
        put(Type.FONT_8, 18);
        put(Type.FONT_9, 19);
        put(Type.STYLE_FRAKTUR, 20);
        put(Type.UNDERLINE_DOUBLE, 21);
        put(Type.INTENSITY_DEFAULT, 22);
        put(Type.STYLE_DEFAULT, 23);
        put(Type.UNDERLINE_NONE, 24);
        put(Type.BLINK_DEFAULT, 25);
        put(Type.PROP_SPACING_ON, 26);
        put(Type.INVERT_OFF, 27);
        put(Type.CONCEAL_OFF, 28);
        put(Type.STRIKE_OFF, 29);
        put(Type.FG_BLK, 30);
        put(Type.FG_RED, 31);
        put(Type.FG_GRN, 32);
        put(Type.FG_YEL, 33);
        put(Type.FG_BLU, 34);
        put(Type.FG_MAG, 35);
        put(Type.FG_CYA, 36);
        put(Type.FG_WHI, 37);
        put(Type.FG_RGB, 38);
        put(Type.FG_DEFAULT, 39);
        put(Type.BG_BLK, 40);
        put(Type.BG_RED, 41);
        put(Type.BG_GRN, 42);
        put(Type.BG_YEL, 43);
        put(Type.BG_BLU, 44);
        put(Type.BG_MAG, 45);
        put(Type.BG_CYA, 46);
        put(Type.BG_WHI, 47);
        put(Type.BG_RGB, 48);
        put(Type.BG_DEFAULT, 49);
        put(Type.PROP_SPACING_OFF, 50);
        put(Type.FRAMED_ON, 51);
        put(Type.ENCIRCLED_ON, 52);
        put(Type.OVERLINED_ON, 53);
        put(Type.FRAMED_ENCIRCLED_OFF, 54);
        put(Type.OVERLINED_OFF, 55);
        put(Type.UNDERLINE_COLOR, 56);
        put(Type.UNDERLINE_COLOR_DEFAULT, 57);
        put(Type.IDEOGRAM_UNDERLINE_ON, 60);
        put(Type.IDEOGRAM_DOUBLE_UNDERLINE_ON, 61);
        put(Type.IDEOGRAM_OVERLINE_ON, 62);
        put(Type.IDEOGRAM_DOUBLE_OVERLINE_ON, 63);
        put(Type.IDEOGRAM_STRESS_MARKING_ON, 64);
        put(Type.IDEOGRAM_OFF, 65);
        put(Type.SUPERSCRIPT, 73);
        put(Type.SUBSCRIPT, 74);
        put(Type.FG_BLK_BRIGHT, 90);
        put(Type.FG_RED_BRIGHT, 91);
        put(Type.FG_GRN_BRIGHT, 92);
        put(Type.FG_YEL_BRIGHT, 93);
        put(Type.FG_BLU_BRIGHT, 94);
        put(Type.FG_MAG_BRIGHT, 95);
        put(Type.FG_CYA_BRIGHT, 96);
        put(Type.FG_WHI_BRIGHT, 97);
        put(Type.BG_BLK_BRIGHT, 100);
        put(Type.BG_RED_BRIGHT, 101);
        put(Type.BG_GRN_BRIGHT, 102);
        put(Type.BG_YEL_BRIGHT, 103);
        put(Type.BG_BLU_BRIGHT, 104);
        put(Type.BG_MAG_BRIGHT, 105);
        put(Type.BG_CYA_BRIGHT, 106);
        put(Type.BG_WHI_BRIGHT, 107);
    }};
}
