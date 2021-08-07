package net.accela.prismatic.sequence;

import net.accela.prismatic.sequence.annotation.Inconsistent;
import net.accela.prismatic.sequence.annotation.NonStandard;
import net.accela.prismatic.sequence.annotation.NotWidelySupported;
import net.accela.prismatic.sequence.annotation.RequiresArgument;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public enum SGRAttribute {
    // Reset
    /**
     * Resets all attributes
     */
    RESET(0),

    // Color Intensity
    /**
     * Enables Bright color intensity OR bold font faces (this can vary between terminals).
     *
     * @see SGRAttribute#INTENSITY_DIM_OR_THIN
     * @see SGRAttribute#INTENSITY_OFF
     */
    @Inconsistent(reason = "Bright or bold")
    INTENSITY_BRIGHT_OR_BOLD(1),

    /**
     * Enables dim color intensity OR faint font faces (this can vary between terminals).
     *
     * @see SGRAttribute#INTENSITY_BRIGHT_OR_BOLD
     * @see SGRAttribute#INTENSITY_OFF
     */
    @Inconsistent(reason = "Dim or faint/thin")
    INTENSITY_DIM_OR_THIN(2),

    /**
     * Resets color intensity back to normal
     *
     * @see SGRAttribute#INTENSITY_BRIGHT_OR_BOLD
     * @see SGRAttribute#INTENSITY_DIM_OR_THIN
     */
    INTENSITY_OFF(22),

    // Emphasis
    /**
     * Enables italic emphasis. Not widely supported. Sometimes treated as an inverse or blink effect.
     *
     * @see SGRAttribute#EMPHASIS_FRAKTUR
     * @see SGRAttribute#EMPHASIS_OFF
     */
    @NotWidelySupported
    @Inconsistent(reason = "Sometimes treated as inverse or blink")
    EMPHASIS_ITALIC(3),

    /**
     * Enables fraktur emphasis. Rarely supported.
     *
     * @see SGRAttribute#EMPHASIS_ITALIC
     * @see SGRAttribute#EMPHASIS_OFF
     */
    @NotWidelySupported
    EMPHASIS_FRAKTUR(20),

    /**
     * Resets emphasis back to normal. Disables italic and fraktur styles
     *
     * @see SGRAttribute#EMPHASIS_ITALIC
     * @see SGRAttribute#EMPHASIS_FRAKTUR
     */
    EMPHASIS_OFF(23),

    // Inversion
    /**
     * Enables color inversion by swapping the foreground and background colors.
     * Also known as "reverse video". Inconsistent emulation.
     *
     * @see SGRAttribute#INVERT_OFF
     */
    @Inconsistent(reason = "Inconsistent emulation")
    INVERT_ON(7),

    /**
     * Disables the color inversion effect; disables swapping foreground and background colors.
     *
     * @see SGRAttribute#INVERT_ON
     */
    @Inconsistent(reason = "Inconsistent emulation")
    INVERT_OFF(27),

    // Concealment
    /**
     * Enables text concealing.
     * Also known as "Hide". Not widely supported.
     *
     * @see SGRAttribute#CONCEAL_OFF
     */
    @NotWidelySupported
    CONCEAL_ON(8),

    /**
     * Disable text concealing.
     *
     * @see SGRAttribute#CONCEAL_ON
     */
    CONCEAL_OFF(28),

    // Strikethrough
    /**
     * Enables a strike-through line across text. Also known as "Crossed out".
     * Makes characters legible, but marked as if for deletion.
     *
     * @see SGRAttribute#STRIKE_THROUGH_OFF
     */
    @NotWidelySupported
    STRIKE_THROUGH_ON(9),

    /**
     * Disables strike-through lines across text.
     *
     * @see SGRAttribute#STRIKE_THROUGH_ON
     */
    @NotWidelySupported
    STRIKE_THROUGH_OFF(29),

    // Cursor blinking
    /**
     * Enables slow cursor blinking. Less than 150 blinks per minute, according to the spec.
     *
     * @see SGRAttribute#BLINK_FAST
     * @see SGRAttribute#BLINK_OFF
     */
    BLINK_SLOW(5),

    /**
     * Enables fast cursor blinking. More than 150 blinks per minute, according to the spec.
     *
     * @see SGRAttribute#BLINK_SLOW
     * @see SGRAttribute#BLINK_OFF
     */
    @NotWidelySupported
    BLINK_FAST(6),

    /**
     * Disables cursor blinking.
     *
     * @see SGRAttribute#BLINK_SLOW
     * @see SGRAttribute#BLINK_FAST
     */
    BLINK_OFF(25),

    // Underlines
    /**
     * Enables a single underline.
     *
     * @see SGRAttribute#UNDERLINE_DOUBLE
     * @see SGRAttribute#UNDERLINE_OFF
     */
    UNDERLINE_SINGLE(4),

    /**
     * Enables a double underline.
     *
     * @see SGRAttribute#UNDERLINE_SINGLE
     * @see SGRAttribute#UNDERLINE_OFF
     */
    @Inconsistent(reason = "Sometimes used as bold off")
    UNDERLINE_DOUBLE(21),

    /**
     * Disables underline.
     *
     * @see SGRAttribute#UNDERLINE_SINGLE
     * @see SGRAttribute#UNDERLINE_DOUBLE
     */
    UNDERLINE_OFF(24),

    // Underline Color
    /**
     * Sets the underline color. Requires a color argument. Nonstandard; not always supported.
     *
     * @see SGRAttribute#UNDERLINE_SINGLE
     * @see SGRAttribute#UNDERLINE_DOUBLE
     * @see SGRAttribute#UNDERLINE_OFF
     * @see SGRAttribute#UNDERLINE_COLOR_DEFAULT
     */
    @NonStandard(info = "Implemented by Kitty, VTE, mintty, and iTerm2. Not in standard.")
    @RequiresArgument(info = "5;n or 2;r;g;b")
    UNDERLINE_COLOR(58, 2, 4),

    /**
     * Resets the underline color. Nonstandard; not always supported.
     *
     * @see SGRAttribute#UNDERLINE_SINGLE
     * @see SGRAttribute#UNDERLINE_DOUBLE
     * @see SGRAttribute#UNDERLINE_OFF
     * @see SGRAttribute#UNDERLINE_COLOR
     */
    @NonStandard(info = "Implemented by Kitty, VTE, mintty, and iTerm2. Not in standard.")
    UNDERLINE_COLOR_DEFAULT(59),

    // Fonts
    /**
     * Enables the default font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_DEFAULT(10),

    /**
     * Enables the 1st alternative font.
     *
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_1(11),

    /**
     * Enables the 2nd alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_2(12),

    /**
     * Enables the 3nd alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_3(13),

    /**
     * Enables the 4th alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_4(14),

    /**
     * Enables the 5th alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_5(15),

    /**
     * Enables the 6th alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_6(16),

    /**
     * Enables the 7th alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_7(17),

    /**
     * Enables the 8th alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_9
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_8(18),

    /**
     * Enables the 9th alternative font.
     *
     * @see SGRAttribute#FONT_1
     * @see SGRAttribute#FONT_2
     * @see SGRAttribute#FONT_3
     * @see SGRAttribute#FONT_4
     * @see SGRAttribute#FONT_5
     * @see SGRAttribute#FONT_6
     * @see SGRAttribute#FONT_7
     * @see SGRAttribute#FONT_8
     * @see SGRAttribute#FONT_DEFAULT
     */
    @Inconsistent(reason = "Largely undefined behaviour. Inconsistent emulation.")
    @NotWidelySupported
    FONT_9(19),

    // Foreground colors
    /**
     * Enables black foreground color.
     *
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_BLK(30),

    /**
     * Enables red foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_RED(31),

    /**
     * Enables green foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_GRN(32),

    /**
     * Enables yellow foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_YEL(33),

    /**
     * Enables blue foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_BLU(34),

    /**
     * Enables magenta foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_MAG(35),

    /**
     * Enables cyan foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_CYA(36),

    /**
     * Enables white foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_RGB
     * @see SGRAttribute#FG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_WHI(37),

    /**
     * Enables RGBStandard foreground color. Requires an argument.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_DEFAULT
     */
    @RequiresArgument(info = "5;n or 2;r;g;b")
    @NotWidelySupported
    FG_RGB(38, 2, 4),

    /**
     * Enables default foreground color.
     *
     * @see SGRAttribute#FG_BLK
     * @see SGRAttribute#FG_RED
     * @see SGRAttribute#FG_GRN
     * @see SGRAttribute#FG_YEL
     * @see SGRAttribute#FG_BLU
     * @see SGRAttribute#FG_MAG
     * @see SGRAttribute#FG_CYA
     * @see SGRAttribute#FG_WHI
     * @see SGRAttribute#FG_RGB
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_DEFAULT(39),

    // Background colors
    /**
     * Enables black background color.
     *
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_BLK(40),

    /**
     * Enables red background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_RED(41),

    /**
     * Enables green background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_GRN(42),

    /**
     * Enables yellow background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_YEL(43),

    /**
     * Enables blue background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_BLU(44),

    /**
     * Enables magenta background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_MAG(45),

    /**
     * Enables cyan background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_CYA(46),

    /**
     * Enables white background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_RGB
     * @see SGRAttribute#BG_DEFAULT
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_WHI(47),

    /**
     * Enables RGBStandard background color. Requires an argument.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_DEFAULT
     */
    @RequiresArgument(info = "5;n or 2;r;g;b")
    @NotWidelySupported
    BG_RGB(48, 2, 4),

    /**
     * Enables default background color.
     *
     * @see SGRAttribute#BG_BLK
     * @see SGRAttribute#BG_RED
     * @see SGRAttribute#BG_GRN
     * @see SGRAttribute#BG_YEL
     * @see SGRAttribute#BG_BLU
     * @see SGRAttribute#BG_MAG
     * @see SGRAttribute#BG_CYA
     * @see SGRAttribute#BG_WHI
     * @see SGRAttribute#BG_RGB
     */
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_DEFAULT(49),

    // Bright foreground colors
    /**
     * Enables bright black foreground.
     *
     * @see SGRAttribute#FG_RED_BRIGHT
     * @see SGRAttribute#FG_GRN_BRIGHT
     * @see SGRAttribute#FG_YEL_BRIGHT
     * @see SGRAttribute#FG_BLU_BRIGHT
     * @see SGRAttribute#FG_MAG_BRIGHT
     * @see SGRAttribute#FG_CYA_BRIGHT
     * @see SGRAttribute#FG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_BLK_BRIGHT(90),

    /**
     * Enables bright red foreground.
     *
     * @see SGRAttribute#FG_BLK_BRIGHT
     * @see SGRAttribute#FG_GRN_BRIGHT
     * @see SGRAttribute#FG_YEL_BRIGHT
     * @see SGRAttribute#FG_BLU_BRIGHT
     * @see SGRAttribute#FG_MAG_BRIGHT
     * @see SGRAttribute#FG_CYA_BRIGHT
     * @see SGRAttribute#FG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_RED_BRIGHT(91),

    /**
     * Enables bright green foreground.
     *
     * @see SGRAttribute#FG_BLK_BRIGHT
     * @see SGRAttribute#FG_RED_BRIGHT
     * @see SGRAttribute#FG_YEL_BRIGHT
     * @see SGRAttribute#FG_BLU_BRIGHT
     * @see SGRAttribute#FG_MAG_BRIGHT
     * @see SGRAttribute#FG_CYA_BRIGHT
     * @see SGRAttribute#FG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_GRN_BRIGHT(92),

    /**
     * Enables bright yellow foreground.
     *
     * @see SGRAttribute#FG_BLK_BRIGHT
     * @see SGRAttribute#FG_RED_BRIGHT
     * @see SGRAttribute#FG_GRN_BRIGHT
     * @see SGRAttribute#FG_BLU_BRIGHT
     * @see SGRAttribute#FG_MAG_BRIGHT
     * @see SGRAttribute#FG_CYA_BRIGHT
     * @see SGRAttribute#FG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_YEL_BRIGHT(93),

    /**
     * Enables bright blue foreground.
     *
     * @see SGRAttribute#FG_BLK_BRIGHT
     * @see SGRAttribute#FG_RED_BRIGHT
     * @see SGRAttribute#FG_GRN_BRIGHT
     * @see SGRAttribute#FG_YEL_BRIGHT
     * @see SGRAttribute#FG_MAG_BRIGHT
     * @see SGRAttribute#FG_CYA_BRIGHT
     * @see SGRAttribute#FG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_BLU_BRIGHT(94),

    /**
     * Enables bright magenta foreground.
     *
     * @see SGRAttribute#FG_BLK_BRIGHT
     * @see SGRAttribute#FG_RED_BRIGHT
     * @see SGRAttribute#FG_GRN_BRIGHT
     * @see SGRAttribute#FG_YEL_BRIGHT
     * @see SGRAttribute#FG_BLU_BRIGHT
     * @see SGRAttribute#FG_CYA_BRIGHT
     * @see SGRAttribute#FG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_MAG_BRIGHT(95),

    /**
     * Enables bright cyan foreground.
     *
     * @see SGRAttribute#FG_BLK_BRIGHT
     * @see SGRAttribute#FG_RED_BRIGHT
     * @see SGRAttribute#FG_GRN_BRIGHT
     * @see SGRAttribute#FG_YEL_BRIGHT
     * @see SGRAttribute#FG_BLU_BRIGHT
     * @see SGRAttribute#FG_MAG_BRIGHT
     * @see SGRAttribute#FG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_CYA_BRIGHT(96),

    /**
     * Enables bright white foreground.
     *
     * @see SGRAttribute#FG_BLK_BRIGHT
     * @see SGRAttribute#FG_RED_BRIGHT
     * @see SGRAttribute#FG_GRN_BRIGHT
     * @see SGRAttribute#FG_YEL_BRIGHT
     * @see SGRAttribute#FG_BLU_BRIGHT
     * @see SGRAttribute#FG_MAG_BRIGHT
     * @see SGRAttribute#FG_CYA_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    FG_WHI_BRIGHT(97),

    // Bright background colors
    /**
     * Enables bright black background.
     *
     * @see SGRAttribute#BG_RED_BRIGHT
     * @see SGRAttribute#BG_GRN_BRIGHT
     * @see SGRAttribute#BG_YEL_BRIGHT
     * @see SGRAttribute#BG_BLU_BRIGHT
     * @see SGRAttribute#BG_MAG_BRIGHT
     * @see SGRAttribute#BG_CYA_BRIGHT
     * @see SGRAttribute#BG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_BLK_BRIGHT(100),

    /**
     * Enables bright red background.
     *
     * @see SGRAttribute#BG_BLK_BRIGHT
     * @see SGRAttribute#BG_GRN_BRIGHT
     * @see SGRAttribute#BG_YEL_BRIGHT
     * @see SGRAttribute#BG_BLU_BRIGHT
     * @see SGRAttribute#BG_MAG_BRIGHT
     * @see SGRAttribute#BG_CYA_BRIGHT
     * @see SGRAttribute#BG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_RED_BRIGHT(101),

    /**
     * Enables bright green background.
     *
     * @see SGRAttribute#BG_BLK_BRIGHT
     * @see SGRAttribute#BG_RED_BRIGHT
     * @see SGRAttribute#BG_YEL_BRIGHT
     * @see SGRAttribute#BG_BLU_BRIGHT
     * @see SGRAttribute#BG_MAG_BRIGHT
     * @see SGRAttribute#BG_CYA_BRIGHT
     * @see SGRAttribute#BG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_GRN_BRIGHT(102),

    /**
     * Enables bright yellow background.
     *
     * @see SGRAttribute#BG_BLK_BRIGHT
     * @see SGRAttribute#BG_RED_BRIGHT
     * @see SGRAttribute#BG_GRN_BRIGHT
     * @see SGRAttribute#BG_BLU_BRIGHT
     * @see SGRAttribute#BG_MAG_BRIGHT
     * @see SGRAttribute#BG_CYA_BRIGHT
     * @see SGRAttribute#BG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_YEL_BRIGHT(103),

    /**
     * Enables bright blue background.
     *
     * @see SGRAttribute#BG_BLK_BRIGHT
     * @see SGRAttribute#BG_RED_BRIGHT
     * @see SGRAttribute#BG_GRN_BRIGHT
     * @see SGRAttribute#BG_YEL_BRIGHT
     * @see SGRAttribute#BG_MAG_BRIGHT
     * @see SGRAttribute#BG_CYA_BRIGHT
     * @see SGRAttribute#BG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_BLU_BRIGHT(104),

    /**
     * Enables bright magenta background.
     *
     * @see SGRAttribute#BG_BLK_BRIGHT
     * @see SGRAttribute#BG_RED_BRIGHT
     * @see SGRAttribute#BG_GRN_BRIGHT
     * @see SGRAttribute#BG_YEL_BRIGHT
     * @see SGRAttribute#BG_BLU_BRIGHT
     * @see SGRAttribute#BG_CYA_BRIGHT
     * @see SGRAttribute#BG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_MAG_BRIGHT(105),

    /**
     * Enables bright cyan background.
     *
     * @see SGRAttribute#BG_BLK_BRIGHT
     * @see SGRAttribute#BG_RED_BRIGHT
     * @see SGRAttribute#BG_GRN_BRIGHT
     * @see SGRAttribute#BG_YEL_BRIGHT
     * @see SGRAttribute#BG_BLU_BRIGHT
     * @see SGRAttribute#BG_MAG_BRIGHT
     * @see SGRAttribute#BG_WHI_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_CYA_BRIGHT(106),

    /**
     * Enables bright white background.
     *
     * @see SGRAttribute#BG_BLK_BRIGHT
     * @see SGRAttribute#BG_RED_BRIGHT
     * @see SGRAttribute#BG_GRN_BRIGHT
     * @see SGRAttribute#BG_YEL_BRIGHT
     * @see SGRAttribute#BG_BLU_BRIGHT
     * @see SGRAttribute#BG_MAG_BRIGHT
     * @see SGRAttribute#BG_CYA_BRIGHT
     */
    @NonStandard(info = "Extension by aixterm, not in standard.")
    @Inconsistent(reason = "These colors are often overridden by terminals")
    BG_WHI_BRIGHT(107),

    // Proportional spacing
    /**
     * Enables proportional spacing
     */
    @NotWidelySupported(info = "ITU T.61 and T.416, not known to be used on terminals.")
    PROP_SPACING_ON(26),
    /**
     * Disables proportional spacing
     */
    @NotWidelySupported(info = "ITU T.61 and T.416, not known to be used on terminals.")
    PROP_SPACING_OFF(50),

    // Framing
    @NotWidelySupported
    @Inconsistent(reason = "Implemented as \"emoji variation selector\" in mintty.")
    FRAMED_ON(51),

    @NotWidelySupported
    @Inconsistent(reason = "Implemented as \"emoji variation selector\" in mintty.")
    ENCIRCLED_ON(52),

    @NotWidelySupported
    OVERLINED_ON(53),

    @NotWidelySupported
    FRAMED_ENCIRCLED_OFF(54),

    @NotWidelySupported
    OVERLINED_OFF(55),

    // Ideogram
    @NotWidelySupported
    IDEOGRAM_UNDERLINE_ON(60),

    @NotWidelySupported
    IDEOGRAM_DOUBLE_UNDERLINE_ON(61),

    @NotWidelySupported
    IDEOGRAM_OVERLINE_ON(62),

    @NotWidelySupported
    IDEOGRAM_DOUBLE_OVERLINE_ON(63),

    @NotWidelySupported
    IDEOGRAM_STRESS_MARKING_ON(64),

    @NotWidelySupported
    IDEOGRAM_OFF(65),

    // Subscript and superscript
    /**
     * Enables subscript. Not widely supported, not in standard. Supported in mintty.
     */
    @NotWidelySupported
    @NonStandard(info = "Extension by mintty, not in standard.")
    SUBSCRIPT(73),

    /**
     * Enables superscript. Not widely supported, not in standard. Supported in mintty.
     */
    @NotWidelySupported
    @NonStandard(info = "Extension by mintty, not in standard.")
    SUPERSCRIPT(74);

    // Global properties
    // Map of all attributes
    private final static Map<@NotNull Integer, @NotNull SGRAttribute> codeMap = new HashMap<>();

    // Attribute properties
    public final int code;
    final int minArgAmount;
    final int maxArgAmount;

    //
    // Constructors
    //

    SGRAttribute(int code) {
        this(code, 0, 0);
    }

    SGRAttribute(int code, int minArgAmount, int maxArgAmount) {
        this.code = code;
        this.minArgAmount = minArgAmount;
        this.maxArgAmount = maxArgAmount;

        // Verify
        if (minArgAmount < 0 || minArgAmount > 4 || maxArgAmount < 0 || maxArgAmount > 4)
            throw new IndexOutOfBoundsException("Min/Max argument amounts must be within 0 and 4.");
        if (minArgAmount > maxArgAmount)
            throw new IndexOutOfBoundsException("Min argument amount must be lower than Max argument amount.");
    }

    //
    // Getters and Inquiries
    //

    public int getCode() {
        return code;
    }

    public byte[] getBytes(@NotNull Charset charset) {
        return String.valueOf(code).getBytes(charset);
    }

    /**
     * @return Whether this requires arguments to be used
     */
    public boolean requiresArguments() {
        return minArgAmount > 0;
        //return code == 38 || code == 48 || code == 58;
    }

    /**
     * @return Whether this can use arguments or not
     */
    public boolean usesArguments() {
        return maxArgAmount > 0;
        //return code == 38 || code == 48 || code == 58;
    }

    /**
     * @return Minimum allowed number of arguments
     */
    public int getMinArgAmount() {
        return minArgAmount;
    }

    /**
     * @return Maximum allowed number of arguments
     */
    public int getMaxArgAmount() {
        return maxArgAmount;
    }

    /**
     * @return if this is a color. Does not include underline colors.
     */
    public boolean isTextColor() {
        return ((code >= 30 && code <= 49) ||
                (code >= 90 && code <= 97) ||
                (code >= 100 && code <= 107));
    }

    /**
     * @return if this is an underline color.
     */
    public boolean isUnderlineColor() {
        return code == 58 || code == 59;
    }

    /**
     * @return if this is a color. Includes underline colors.
     */
    public boolean isAnyColor() {
        return ((code >= 30 && code <= 49) ||
                (code >= 90 && code <= 97) ||
                (code >= 100 && code <= 107)) ||
                (code == 58 || code == 59);
    }

    /**
     * @return if this is a foreground color.
     */
    public boolean isForegroundColor() {
        return code >= FG_BLK.code && code <= FG_DEFAULT.code;
    }

    /**
     * @return if this is a background color.
     */
    public boolean isBackgroundColor() {
        return code >= BG_BLK.code && code <= BG_DEFAULT.code;
    }

    //
    // Static utility methods
    //

    /**
     * This is better to use than values()[index],
     * as this uses the actual SGR indices instead of array indices.
     *
     * @param code The index to look for
     * @return The matching {@link SGRAttribute}, or null if not found.
     */
    public static @NotNull SGRAttribute fromInt(int code) {
        // Populate the hashmap if that's not been dealt with already.
        synchronized (codeMap) {
            if (codeMap.size() < 1) {
                for (SGRAttribute value : values()) {
                    codeMap.put(value.getCode(), value);
                }
            }
        }

        // Return the SGR at this index
        SGRAttribute attribute = codeMap.get(code);
        if (attribute == null) {
            throw new IllegalArgumentException(String.format("Unknown SGRAttribute code '%s'", code));
        }
        return attribute;
    }
}
