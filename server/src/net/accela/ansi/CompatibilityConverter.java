package net.accela.ansi;

import net.accela.ansi.exception.ESCSequenceException;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.SGRStatement;
import net.accela.ansi.sequence.color.PaletteColor;
import net.accela.ansi.sequence.color.StandardColor;
import net.accela.ansi.sequence.color.standard.RGB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Makes it easy to maintain compatibility, by adding tools for converting
 * incompatible {@link SGRSequence}s into their compatible equivalents.
 */
public class CompatibilityConverter {
    boolean supportsICEColor;
    boolean supportsAixtermColor;
    boolean supportsRGBColor;

    public CompatibilityConverter(boolean ICEColor, boolean RGBColor, boolean AixtermColor) {
        this.supportsICEColor = ICEColor;
        this.supportsAixtermColor = AixtermColor;
        this.supportsRGBColor = RGBColor;
    }

    public void setSupportsICEColor(boolean supportsICEColor) {
        this.supportsICEColor = supportsICEColor;
    }

    public void setSupportsAixtermColor(boolean supportsAixtermColor) {
        this.supportsAixtermColor = supportsAixtermColor;
    }

    public void setSupportsRGBColor(boolean supportsRGBColor) {
        this.supportsRGBColor = supportsRGBColor;
    }

    public boolean supportsICEColor() {
        return supportsICEColor;
    }

    public boolean supportsAixtermColor() {
        return supportsAixtermColor;
    }

    public boolean supportsRGBColor() {
        return supportsRGBColor;
    }

    public @NotNull String makeCompatible(final @NotNull List<@NotNull SGRStatement> statements) {
        final StringBuilder result = new StringBuilder(AnsiLib.CSI);
        SGRStatement.Type intensityType = SGRStatement.Type.INTENSITY_DEFAULT;

        for (SGRStatement statement : statements) {
            Boolean bright = null;

            switch (statement.getType()) {
                case INTENSITY_DEFAULT:
                case INTENSITY_BRIGHT_OR_BOLD:
                case INTENSITY_DIM_OR_THIN:
                    intensityType = statement.getType();
                    if (!supportsICEColor) result.append(statement.getTypeAsInt()).append(';');
                    break;
                case FG_BLK_BRIGHT:
                case FG_RED_BRIGHT:
                case FG_GRN_BRIGHT:
                case FG_YEL_BRIGHT:
                case FG_BLU_BRIGHT:
                case FG_MAG_BRIGHT:
                case FG_CYA_BRIGHT:
                case FG_WHI_BRIGHT:
                case BG_BLK_BRIGHT:
                case BG_RED_BRIGHT:
                case BG_GRN_BRIGHT:
                case BG_YEL_BRIGHT:
                case BG_BLU_BRIGHT:
                case BG_MAG_BRIGHT:
                case BG_CYA_BRIGHT:
                case BG_WHI_BRIGHT:
                    bright = true;
                case FG_BLK:
                case FG_RED:
                case FG_GRN:
                case FG_YEL:
                case FG_BLU:
                case FG_MAG:
                case FG_CYA:
                case FG_WHI:
                case BG_BLK:
                case BG_RED:
                case BG_GRN:
                case BG_YEL:
                case BG_BLU:
                case BG_MAG:
                case BG_CYA:
                case BG_WHI:
                    if (bright == null) {
                        bright = intensityType == SGRStatement.Type.INTENSITY_BRIGHT_OR_BOLD && supportsICEColor;
                    }
                    parseStandardColor(new StandardColor(statement.getType(), bright), result);
                    break;
                case FG_RGB:
                case BG_RGB:
                    parseRGB(statement, result);
                    break;
                default:
                    result.append(statement.getTypeAsInt()).append(';');
                    for (int arg : statement.getArguments()) {
                        result.append(arg).append(';');
                    }
                    break;
            }
        }
        return result.deleteCharAt(result.length() - 1).append('m').toString();
    }

    void parseRGB(final @NotNull SGRStatement statement, final @NotNull StringBuilder result) {
        if (supportsRGBColor) {
            result.append(statement.getTypeAsInt()).append(';');
            for (int arg : statement.getArguments()) {
                result.append(arg).append(';');
            }
        } else {
            boolean fg = statement.getType() == SGRStatement.Type.FG_RGB;
            int[] args = statement.getArguments();
            RGB rgb;
            if (args[0] == 5) {
                rgb = new PaletteColor(args[1]).toRGB();
            } else if (args[0] == 2) {
                rgb = new RGB(args[1], args[2], args[3]);
            } else {
                throw new ESCSequenceException("Unknown color type '" + args[0] + "'");
            }
            parseStandardColor(new StandardColor(rgb, fg), result);
        }
    }

    void parseStandardColor(final @NotNull StandardColor standardColor, final @NotNull StringBuilder result) {
        SGRSequence colorSequence = new SGRSequence(standardColor.toString(supportsAixtermColor));

        for (SGRStatement colorStatement : colorSequence.toSGRStatements()) {
            result.append(colorStatement.getTypeAsInt()).append(';');
            for (int arg : colorStatement.getArguments()) {
                result.append(arg).append(';');
            }
        }
    }
}
