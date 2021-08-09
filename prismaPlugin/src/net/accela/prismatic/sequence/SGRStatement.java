package net.accela.prismatic.sequence;

import net.accela.prismatic.ui.text.color.TextColor;
import net.accela.prismatic.util.ANSIPatterns;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class SGRStatement {
    @NotNull
    final SGRAttribute attribute;
    final int[] arguments;

    //
    // Constructors
    //

    private SGRStatement(@NotNull SGRAttribute attribute) {
        this(attribute, null);
    }

    private SGRStatement(@NotNull SGRAttribute attribute, int[] arguments) {
        this.attribute = attribute;
        this.arguments = arguments;
    }

    //
    // Factory methods
    //

    public static @NotNull SGRStatement[] fromSGRAttribute(final SGRAttribute attribute) {
        return fromSGRIntArray(new int[]{attribute.getCode()});
    }

    public static @NotNull SGRStatement[] fromSGRAttribute(final SGRAttribute attribute, int[] arguments) {
        int[] sequence = new int[1 + arguments.length];
        sequence[0] = attribute.getCode();
        System.arraycopy(arguments, 0, sequence, 1, sequence.length - 1);
        return fromSGRIntArray(sequence);
    }

    public static @NotNull SGRStatement[] fromSGRIntArray(final int[] sequence) {
        // Convert to SGRStatements
        // Catch ESC[m (no integers), which is a short form of ESC[0m
        if (sequence.length == 0) {
            return new SGRStatement[]{new SGRStatement(SGRAttribute.RESET)};
        }
        // Single statement with no argument
        else if (sequence.length == 1) {
            return new SGRStatement[]{new SGRStatement(SGRAttribute.fromInt(sequence[0]))};
        }
        // A single statement with arguments, or multiple statements, possibly with arguments
        else {
            SGRStatement[] statements = new SGRStatement[0];
            for (int i = 0; i < sequence.length; i++) {
                // Create attribute
                final SGRAttribute attribute = SGRAttribute.fromInt(sequence[i]);

                // If it has arguments
                if (attribute.usesArguments()) {
                    try {
                        // If it's a color
                        if (attribute.isAnyColor()) {
                            // Advance by one
                            i++;
                            int rgbType = sequence[i];

                            // Decide on argument array length
                            final int[] args;
                            args = switch (rgbType) {
                                case 5 -> new int[2];
                                case 2 -> new int[4];
                                default -> throw new IllegalArgumentException(String.format(
                                        "Unknown RGB color type '%s' for attribute '%s'",
                                        rgbType, attribute
                                ));
                            };

                            // Populate the argument array
                            int argsEndIndex = i + args.length - 1;
                            int argsArrayIndex = 0;
                            while (i <= argsEndIndex) {
                                int rgbValue = sequence[i];

                                // Verify
                                if (rgbValue < 0 || rgbValue > 255)
                                    throw new IllegalArgumentException(
                                            String.format("Invalid RGBStandard value: '%s'", rgbValue));

                                // Add
                                args[argsArrayIndex] = rgbValue;
                                i++;
                                argsArrayIndex++;
                            }

                            // Form a statement
                            SGRStatement statement = new SGRStatement(attribute, args);

                            // Extend and add to array
                            statements = Arrays.copyOf(statements, statement.arguments.length + 1);
                            statements[statements.length - 1] = statement;

                        } else {
                            // If it has arguments but is not a color
                            throw new IllegalStateException(String.format(
                                    "SGRAttribute '%s' requires argument(s) but isn't a color. Not implemented; " +
                                            "According to the SGR sequence standard at the time of writing, " +
                                            "this should never occur.", attribute));
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        throw new IllegalArgumentException(String.format(
                                "Invalid amount of arguments for attribute '%s'", attribute), ex);
                    }
                }
                // If it has no arguments
                else {
                    // Form a statement
                    SGRStatement statement = new SGRStatement(attribute);

                    // Extend and add to array
                    statements = Arrays.copyOf(statements, statements.length + 1);
                    statements[statements.length - 1] = statement;
                }
            }
            return statements;
        }
    }

    public static @NotNull SGRStatement[] fromSGRString(@NotNull final String sequence) {
        // Match and capture any SGR sequences
        final String[] sgrSeqMatches = ANSIPatterns.SGR_sequenceCapture
                .matcher(sequence)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);

        // Confirm input
        if (sgrSeqMatches.length < 1) throw new IllegalArgumentException("String was not an SGR sequence.");
        if (sgrSeqMatches.length > 1 || sgrSeqMatches[0].length() != sequence.length())
            throw new IllegalArgumentException("String must consist of a single Sequence, with no extra characters.");

        // Extract integers
        final String[] strIntMatches = Pattern.compile("\\d+")
                .matcher(sgrSeqMatches[0])
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);

        // Convert to integers
        final int[] intValues = new int[strIntMatches.length];
        for (int i = 0; i < intValues.length; i++) {
            intValues[i] = Integer.parseInt(strIntMatches[i]);
        }

        // Parse
        return fromSGRIntArray(intValues);
    }

    //
    // Getters
    //

    public @NotNull SGRAttribute getAttribute() {
        return attribute;
    }

    /**
     * @return The arguments accompanying the attribute (if any), as integers.
     */
    public int[] getArgumentsAsInt() {
        return arguments;
    }

    public @NotNull String getStatementAsString() {
        StringBuilder statementSB = new StringBuilder(attribute.getCode());

        for (int argument : arguments) {
            statementSB.append(';').append(argument);
        }

        return statementSB.toString();
    }

    public byte[] getStatementBytes() {
        return getStatementAsString().getBytes();
    }

    /**
     * Converts the {@link SGRStatement} to a {@link TextColor} if supported.
     *
     * @return A {@link TextColor} if this {@link SGRStatement}'s {@link SGRAttribute} is a color.
     * @throws UnsupportedOperationException If the {@link SGRStatement} is not a color.
     */
    public @NotNull TextColor toColor() {
        switch (attribute) {
            case FG_BLK:
            case BG_BLK:
                return TextColor.ANSI.BLACK;
            case FG_RED:
            case BG_RED:
                return TextColor.ANSI.RED;
            case FG_GRN:
            case BG_GRN:
                return TextColor.ANSI.GREEN;
            case FG_YEL:
            case BG_YEL:
                return TextColor.ANSI.YELLOW;
            case FG_BLU:
            case BG_BLU:
                return TextColor.ANSI.BLUE;
            case FG_MAG:
            case BG_MAG:
                return TextColor.ANSI.MAGENTA;
            case FG_CYA:
            case BG_CYA:
                return TextColor.ANSI.CYAN;
            case FG_WHI:
            case BG_WHI:
                return TextColor.ANSI.WHITE;
            case FG_BLK_BRIGHT:
            case BG_BLK_BRIGHT:
                return TextColor.ANSI.BLACK_BRIGHT;
            case FG_RED_BRIGHT:
            case BG_RED_BRIGHT:
                return TextColor.ANSI.RED_BRIGHT;
            case FG_GRN_BRIGHT:
            case BG_GRN_BRIGHT:
                return TextColor.ANSI.GREEN_BRIGHT;
            case FG_YEL_BRIGHT:
            case BG_YEL_BRIGHT:
                return TextColor.ANSI.YELLOW_BRIGHT;
            case FG_BLU_BRIGHT:
            case BG_BLU_BRIGHT:
                return TextColor.ANSI.BLUE_BRIGHT;
            case FG_MAG_BRIGHT:
            case BG_MAG_BRIGHT:
                return TextColor.ANSI.MAGENTA_BRIGHT;
            case FG_CYA_BRIGHT:
            case BG_CYA_BRIGHT:
                return TextColor.ANSI.CYAN_BRIGHT;
            case FG_WHI_BRIGHT:
            case BG_WHI_BRIGHT:
                return TextColor.ANSI.WHITE_BRIGHT;
            case FG_RGB:
            case BG_RGB:
            case UNDERLINE_COLOR:
                StringBuilder sb = new StringBuilder("#");
                for (int i = 1; i < arguments.length; i++) {
                    sb.append(arguments[i]);
                }
                return TextColor.Factory.fromString(sb.toString());
            case FG_DEFAULT:
            case BG_DEFAULT:
            case UNDERLINE_COLOR_DEFAULT:
                return TextColor.ANSI.DEFAULT;
            default:
                throw new UnsupportedOperationException(
                        "This SGRStatement is not a color, or it has not been implemented yet.");
        }
    }
}
