package net.accela.prismatic.sequence;

import net.accela.prismatic.gui.text.color.TextColor;
import net.accela.prismatic.util.ANSIPatterns;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class SGRStatement {
    @NotNull
    final SGRAttribute attribute;
    final int[] arguments;

    //
    // Constructors
    //

    public SGRStatement(@NotNull SGRAttribute attribute) {
        this(attribute, null);
    }

    public SGRStatement(@NotNull SGRAttribute attribute, int[] arguments) {
        this.attribute = attribute;
        this.arguments = arguments;

        int index = attribute.getIndex();

        // Validation

        // Prep some common strings since they're the same across all exceptions.
        final String typeExStr = String.format("SGR attribute '%s' ('%d')", attribute, index);
        final String argsExStr = String.format("Provided arguments: '%s'", Arrays.toString(arguments));

        // If it requires arguments
        if (attribute.requiresArguments()) {
            if (attribute.isAnyColor()) {
                if (arguments == null) throw new IllegalArgumentException(
                        String.format("%s requires arguments. %s", typeExStr, argsExStr));

                // Validate arguments length
                try {
                    // Figure out which length is valid
                    int validArgumentsLength;
                    int initiator = arguments[0];
                    switch (initiator) {
                        case 5:
                            validArgumentsLength = 1;
                            break;
                        case 2:
                            validArgumentsLength = 3;
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    String.format("Unknown colour type '%d' for %s", initiator, typeExStr));
                    }

                    // Calculate length
                    for (int i = 1; i <= validArgumentsLength; i++) {
                        int rgbArg = arguments[i];
                        if (rgbArg < 0 || rgbArg > 255)
                            throw new IllegalArgumentException(
                                    String.format("Invalid RGBStandard value: '%s'", rgbArg));
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    throw new IllegalArgumentException(
                            String.format("Too few arguments for %s. %s", typeExStr, argsExStr));
                }
            }
            // If it requires arguments but isn't a color then we don't know what to do with it
            // (At the time of writing there's no such thing in the spec), so we should throw an exception.
            else {
                throw new IllegalArgumentException(String.format(
                        "SGRAttribute '%s' requires argument(s) but isn't a color", attribute
                ));
            }
        }
        // Anything else that does not accept arguments
        else if (arguments != null) throw new IllegalArgumentException(String.format(
                "%s does not support arguments. %s", typeExStr, argsExStr
        ));
    }

    public static @NotNull Set<@NotNull SGRStatement> fromSGRString(@NotNull String sequence) {
        // Filter out ESC (\x1B) and/or '[' at the start, as well as 'm' at the end
        final String[] sgrSeqMatches = ANSIPatterns.SGR_sequenceCapture
                .matcher(sequence)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
        if (sgrSeqMatches.length == 0) return new HashSet<>();

        final String[] intMatches = Pattern.compile("(\\d+)")
                .matcher(sequence)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
        if (intMatches.length == 0) return new HashSet<>() {{
            add(new SGRStatement(SGRAttribute.RESET));
        }};

        // Convert to integers
        final List<Integer> intValues = new ArrayList<>();
        for (String string : intMatches) {
            intValues.add(Integer.parseInt(string));
        }

        // Parse values into a List of SGRStatements
        Set<SGRStatement> statements = new HashSet<>();
        try {
            for (int index = 0; index < intValues.size(); index++) {
                final int typeAsInt = intValues.get(index);
                // If it has arguments
                // fixme migrate to using the methods provided by SGRAttribute for arguments.
                if (typeAsInt == 38 | typeAsInt == 48 | typeAsInt == 58) {
                    // Advance by one
                    index++;
                    int rgbType = intValues.get(index);

                    // Decide on argument array length
                    final int[] args;
                    if (rgbType == 2) args = new int[4];
                    else if (rgbType == 5) args = new int[2];
                    else throw new IllegalArgumentException("Unknown rgb color type: '" + rgbType + "'");

                    // Populate the argument array
                    int argsEndIndex = index + args.length - 1;
                    int argsArrayIndex = 0;
                    while (index <= argsEndIndex) {
                        int rgbValue = intValues.get(index);
                        // Note that we do not need to check whether the arguments are valid -
                        // that is done automatically upon SGRStatement creation.
                        args[argsArrayIndex] = rgbValue;

                        index++;
                        argsArrayIndex++;
                    }

                    // Form a statement
                    SGRStatement statement = new SGRStatement(SGRAttribute.fromIndex(typeAsInt), args);
                    statements.add(statement);
                }
                // If it has no arguments
                else {
                    statements.add(new SGRStatement(SGRAttribute.fromIndex(typeAsInt)));
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(String.format(
                    "Invalid amount of arguments for sequence type. Sequence: %s", sequence
            ), ex);
        }
        return statements;
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
        StringBuilder statementSB = new StringBuilder(attribute.getIndex());

        for (int argument : arguments) {
            statementSB.append(';').append(argument);
        }

        return statementSB.toString();
    }

    public byte[] getStatementBytes() {
        return getStatementAsString().getBytes();
    }

    /**
     * If this {@link SGRStatement}'s {@link SGRAttribute} isn't a color,
     * or if the color is default then null will be returned.
     *
     * @return A {@link TextColor} if this {@link SGRStatement}'s {@link SGRAttribute} is a color.
     * Null if it's not a color, or if the color is default.
     */
    public @Nullable TextColor toColor() {
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
            default:
                return null;
        }
    }
}
