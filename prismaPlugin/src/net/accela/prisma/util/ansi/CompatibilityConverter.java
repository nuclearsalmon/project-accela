package net.accela.prisma.util.ansi;

import net.accela.ansi.exception.ESCSequenceException;
import net.accela.ansi.sequence.CSISequence;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.SGRStatement;
import net.accela.ansi.sequence.color.StandardColor;
import net.accela.ansi.sequence.color.TableColor;
import net.accela.ansi.sequence.color.standard.RGB;
import net.accela.prisma.session.TerminalAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @deprecated Replaced with {@link SequenceCompressor}.
 * Makes it easy to maintain compatibility, by adding tools for converting
 * incompatible {@link SGRSequence}s into their compatible equivalents.
 */
@Deprecated
public class CompatibilityConverter {
    final TerminalAccessor terminal;

    public CompatibilityConverter(@NotNull TerminalAccessor terminal) {
        this.terminal = terminal;
    }

    public @NotNull String makeCompatible(final @NotNull List<@NotNull SGRStatement> statements) {
        final StringBuilder result = new StringBuilder(CSISequence.CSI_STRING);
        SGRStatement.Type intensityType = SGRStatement.Type.INTENSITY_OFF;

        for (SGRStatement statement : statements) {
            Boolean bright = null;

            switch (statement.getType()) {
                case INTENSITY_OFF:
                case INTENSITY_BRIGHT_OR_BOLD:
                case INTENSITY_DIM_OR_THIN:
                    intensityType = statement.getType();
                    if (!terminal.supportsIceColor()) {
                        result.append(statement.getTypeAsInt()).append(';');
                    }
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
                        bright = intensityType == SGRStatement.Type.INTENSITY_BRIGHT_OR_BOLD
                                && terminal.supportsIceColor();
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
        if (terminal.supportsTrueColor()) {
            result.append(statement.getTypeAsInt()).append(';');
            for (int arg : statement.getArguments()) {
                result.append(arg).append(';');
            }
        } else {
            boolean fg = statement.getType() == SGRStatement.Type.FG_RGB;
            int[] args = statement.getArguments();
            RGB rgb;
            if (args[0] == 5) {
                rgb = new TableColor(args[1]).toRGB();
            } else if (args[0] == 2) {
                rgb = new RGB(args[1], args[2], args[3]);
            } else {
                throw new ESCSequenceException("Unknown color type '" + args[0] + "'");
            }
            parseStandardColor(new StandardColor(rgb, fg), result);
        }
    }

    void parseStandardColor(final @NotNull StandardColor standardColor, final @NotNull StringBuilder result) {
        SGRSequence colorSequence = new SGRSequence(standardColor.toString(terminal.supportsAixtermColor()));

        for (SGRStatement colorStatement : colorSequence.toSGRStatements()) {
            result.append(colorStatement.getTypeAsInt()).append(';');
            for (int arg : colorStatement.getArguments()) {
                result.append(arg).append(';');
            }
        }
    }
}
