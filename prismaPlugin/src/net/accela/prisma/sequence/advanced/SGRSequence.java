package net.accela.prisma.sequence.advanced;

import net.accela.prisma.sequence.SGRAttribute;
import net.accela.prisma.sequence.SGRStatement;
import net.accela.prisma.terminal.chars.C0ControlCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Represents a single immutable ANSI escape sequence SGR (Select Graphics Rendition).
 */
public class SGRSequence extends CSISequence {
    public SGRSequence(@NotNull SGRStatement statement) throws InvalidANSISequenceException {
        this(new ArrayList<SGRStatement>() {{
            add(statement);
        }});
    }

    public SGRSequence(@NotNull SGRStatement[] statements) throws InvalidANSISequenceException {
        this(new ArrayList<>(Arrays.asList(statements)));
    }

    public SGRSequence(@NotNull List<SGRStatement> statements) throws InvalidANSISequenceException {
        this(toString(statements));
    }

    SGRSequence(@NotNull String str) throws InvalidANSISequenceException {
        super(str);
    }

    public static @Nullable SGRSequence fromString(@NotNull String str) {
        try {
            return new SGRSequence(str);
        } catch (InvalidANSISequenceException ignored) {
        }
        return null;
    }

    public @NotNull List<@NotNull SGRStatement> toSGRStatements() throws InvalidANSISequenceException {
        return toSGRStatements(sequenceString);
    }

    public static @NotNull List<@NotNull SGRStatement> toSGRStatements(String sequence) throws InvalidANSISequenceException {
        // Filter out ESC (\x1B) and/or '[' at the start, as well as 'm' at the end, and split by ;
        final String[] matches = Pattern.compile("([^\\x1B\\[;m]+)")
                .matcher(sequence)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);

        // Convert to integers
        final List<Integer> intValues = new ArrayList<>();
        for (String string : matches) {
            intValues.add(Integer.parseInt(string));
        }

        // Parse values into a List of SGRStatements
        List<SGRStatement> statements = new ArrayList<>();
        try {
            for (int index = 0; index < intValues.size(); index++) {
                final int typeAsInt = intValues.get(index);
                // If it has arguments
                if (typeAsInt == 38 | typeAsInt == 48 | typeAsInt == 58) {
                    // Advance by one
                    index++;
                    int rgbType = intValues.get(index);

                    // Decide on argument array length
                    final int[] args;
                    if (rgbType == 2) args = new int[4];
                    else if (rgbType == 5) args = new int[2];
                    else throw new InvalidANSISequenceException("Unknown rgb color type: '" + rgbType + "'");

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
                    SGRStatement statement = new SGRStatement(Objects.requireNonNull(SGRAttribute.fromIndex(typeAsInt)), args);
                    statements.add(statement);
                }
                // If it has no arguments
                else {
                    statements.add(new SGRStatement(Objects.requireNonNull(SGRAttribute.fromIndex(typeAsInt))));
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidANSISequenceException(sequence, "Invalid amount of arguments for sequence type", ex);
        }

        return statements;
    }

    @NotNull
    @Override
    public String toString() {
        return sequenceString;
    }

    @NotNull
    static String toString(@NotNull List<@NotNull SGRStatement> statements) {
        StringBuilder stmtSB = new StringBuilder(C0ControlCode.ESC + "[");

        for (SGRStatement stmt : statements) {
            stmtSB.append(stmt.getAttribute().getIndex());
            int[] stmtArgs = stmt.getArgumentsAsInt();
            if (stmtArgs != null) {
                for (int stmtArg : stmtArgs) {
                    stmtSB.append(';').append(stmtArg);
                }
            }
            stmtSB.append(';');
        }
        if (stmtSB.length() > 2) stmtSB.deleteCharAt(stmtSB.length() - 1);

        stmtSB.append("m");
        return stmtSB.toString();
    }

    /**
     * Compresses a list of statements
     * Ignores some arguments such as framing, ideogram and super/sub-script
     *
     * @param statements A bunch of statements to filter/compress
     * @return The same statements as before, but without any duplicates or unnecessary ones
     */
    public static @NotNull List<@NotNull SGRStatement> compress(@NotNull List<@NotNull SGRStatement> statements) {
        // The statements to be returned
        final List<SGRStatement> compressed = new ArrayList<>();

        // Search for reset marker
        int resetMarker = -1;
        for (SGRStatement statement : statements) {
            if (statement.getAttribute() == SGRAttribute.RESET) {
                resetMarker = statements.indexOf(statement);
            }
        }
        // Cut out everything before the reset, if there's one. Include the reset.
        if (resetMarker != -1 && resetMarker + 1 < statements.size()) {
            statements = statements.subList(resetMarker, statements.size());
        }

        // INTENSITY_BRIGHT, INTENSITY_DIM, INTENSITY_NORMAL
        SGRStatement tmpIntensity = null;
        SGRStatement fgIntensity = null;
        SGRStatement bgIntensity = null;
        // STYLE_ITALIC, STYLE_FRAKTUR, STYLE_NORMAL
        SGRStatement style = null;
        // BLINK_SLOW, BLINK_RAPID, BLINK_NORMAL
        SGRStatement blink = null;
        // INVERT
        SGRStatement invert = null;
        // CONCEAL
        SGRStatement conceal = null;
        // STRIKE
        SGRStatement strike = null;
        // FONT_0, FONT_1, FONT_2, FONT_3, FONT_4, FONT_5, FONT_6, FONT_7, FONT_8, FONT_9
        SGRStatement font = null;
        // UNDERLINE_SINGLE, UNDERLINE_DOUBLE, UNDERLINE_NORMAL
        SGRStatement underline = null;
        // FG Color
        SGRStatement fgColor = null;
        // BG Color
        SGRStatement bgColor = null;
        // Underline Color
        SGRStatement underlineColor = null;
        // PROP_SPACING_ON, PROP_SPACING_OFF
        SGRStatement propSpacing = null;

        // Filter out unnecessary statements
        for (SGRStatement statement : statements) {
            switch (statement.getAttribute()) {
                case RESET:
                    compressed.add(statement);
                    break;
                case INTENSITY_OFF:
                case INTENSITY_BRIGHT_OR_BOLD:
                case INTENSITY_DIM_OR_THIN:
                    tmpIntensity = statement;
                    break;
                case EMPHASIS_OFF:
                case EMPHASIS_ITALIC:
                case EMPHASIS_FRAKTUR:
                    style = statement;
                    break;
                case BLINK_OFF:
                case BLINK_SLOW:
                case BLINK_FAST:
                    blink = statement;
                    break;
                case INVERT_ON:
                case INVERT_OFF:
                    invert = statement;
                    break;
                case CONCEAL_ON:
                case CONCEAL_OFF:
                    conceal = statement;
                    break;
                case STRIKE_THROUGH_ON:
                case STRIKE_THROUGH_OFF:
                    strike = statement;
                    break;
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
                    font = statement;
                    break;
                case UNDERLINE_OFF:
                case UNDERLINE_SINGLE:
                case UNDERLINE_DOUBLE:
                    underline = statement;
                    break;
                case FG_BLK:
                case FG_RED:
                case FG_GRN:
                case FG_YEL:
                case FG_BLU:
                case FG_MAG:
                case FG_CYA:
                case FG_WHI:
                case FG_RGB:
                case FG_DEFAULT:
                case FG_BLK_BRIGHT:
                case FG_RED_BRIGHT:
                case FG_GRN_BRIGHT:
                case FG_YEL_BRIGHT:
                case FG_BLU_BRIGHT:
                case FG_MAG_BRIGHT:
                case FG_CYA_BRIGHT:
                case FG_WHI_BRIGHT:
                    fgColor = statement;
                    if (tmpIntensity != null) {
                        fgIntensity = tmpIntensity;
                        tmpIntensity = null;
                    }
                    break;
                case BG_BLK:
                case BG_RED:
                case BG_GRN:
                case BG_YEL:
                case BG_BLU:
                case BG_MAG:
                case BG_CYA:
                case BG_WHI:
                case BG_RGB:
                case BG_DEFAULT:
                case BG_BLK_BRIGHT:
                case BG_RED_BRIGHT:
                case BG_GRN_BRIGHT:
                case BG_YEL_BRIGHT:
                case BG_BLU_BRIGHT:
                case BG_MAG_BRIGHT:
                case BG_CYA_BRIGHT:
                case BG_WHI_BRIGHT:
                    bgColor = statement;
                    if (tmpIntensity != null) {
                        bgIntensity = tmpIntensity;
                        tmpIntensity = null;
                    }
                    break;
                case UNDERLINE_COLOR:
                case UNDERLINE_COLOR_DEFAULT:
                    underlineColor = statement;
                    break;
                case PROP_SPACING_ON:
                case PROP_SPACING_OFF:
                    propSpacing = statement;
                    break;
            }
        }

        if (font != null) compressed.add(font);
        if (propSpacing != null) compressed.add(propSpacing);
        if (style != null) compressed.add(style);
        if (blink != null) compressed.add(blink);
        if (invert != null) compressed.add(invert);
        if (conceal != null) compressed.add(conceal);
        if (strike != null) compressed.add(strike);
        if (underline != null) compressed.add(underline);
        if (underlineColor != null) compressed.add(underlineColor);
        if (tmpIntensity != null) compressed.add(tmpIntensity);
        if (fgIntensity != null) compressed.add(fgIntensity);
        if (fgColor != null) compressed.add(fgColor);
        if (bgIntensity != null) compressed.add(bgIntensity);
        if (bgColor != null) compressed.add(bgColor);

        return compressed;
    }
}