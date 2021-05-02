package net.accela.prismatic.gui.text.effect;

import net.accela.prismatic.sequence.SGRAttribute;
import net.accela.prismatic.sequence.annotation.Inconsistent;
import net.accela.prismatic.sequence.annotation.NotWidelySupported;
import org.jetbrains.annotations.NotNull;

public enum TerminalFont {
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
    FONT_DEFAULT(SGRAttribute.FONT_DEFAULT),

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
    FONT_1(SGRAttribute.FONT_1),

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
    FONT_2(SGRAttribute.FONT_2),

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
    FONT_3(SGRAttribute.FONT_3),

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
    FONT_4(SGRAttribute.FONT_4),

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
    FONT_5(SGRAttribute.FONT_5),

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
    FONT_6(SGRAttribute.FONT_6),

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
    FONT_7(SGRAttribute.FONT_7),

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
    FONT_8(SGRAttribute.FONT_8),

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
    FONT_9(SGRAttribute.FONT_9);

    final SGRAttribute attribute;

    TerminalFont(@NotNull SGRAttribute attribute) {
        switch (attribute) {
            case FONT_DEFAULT:
            case FONT_1:
            case FONT_2:
            case FONT_3:
            case FONT_4:
            case FONT_5:
            case FONT_6:
            case FONT_7:
            case FONT_8:
            case FONT_9:
                break;
            default:
                throw new IllegalArgumentException("Not a font");
        }
        this.attribute = attribute;
    }

    @NotNull
    public static TerminalFont fromSGRAttribute(@NotNull SGRAttribute attribute) {
        switch (attribute) {
            case FONT_DEFAULT:
                return FONT_DEFAULT;
            case FONT_1:
                return FONT_1;
            case FONT_2:
                return FONT_2;
            case FONT_3:
                return FONT_3;
            case FONT_4:
                return FONT_4;
            case FONT_5:
                return FONT_5;
            case FONT_6:
                return FONT_6;
            case FONT_7:
                return FONT_7;
            case FONT_8:
                return FONT_8;
            case FONT_9:
                return FONT_9;
            default:
                throw new IllegalArgumentException("Not a font");
        }
    }
}
