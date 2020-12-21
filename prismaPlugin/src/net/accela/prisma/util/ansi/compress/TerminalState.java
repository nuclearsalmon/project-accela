package net.accela.prisma.util.ansi.compress;

import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.SGRStatement;
import net.accela.prisma.session.TerminalReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Can be used to record a terminal state,
 * or to compresses and optimize {@link SGRStatement}s, removing any unnecessary statements.
 */
public class TerminalState {
    final TerminalReference terminal;

    /**
     * INTENSITY_BRIGHT, INTENSITY_DIM, INTENSITY_NORMAL
     */
    SGRStatement intensity = null;
    SGRStatement fgIntensity = null;
    SGRStatement bgIntensity = null;
    /**
     * FG Color
     */
    SGRStatement fgColor = null;
    /**
     * BG Color
     */
    SGRStatement bgColor = null;
    /**
     * UNDERLINE_SINGLE, UNDERLINE_DOUBLE, UNDERLINE_NORMAL
     */
    SGRStatement underline = null;
    /**
     * Underline Color
     */
    SGRStatement underlineColor = null;
    /**
     * STRIKE
     */
    SGRStatement strike = null;
    /**
     * INVERT
     */
    SGRStatement invert = null;
    /**
     * STYLE_ITALIC, STYLE_FRAKTUR, STYLE_NORMAL
     */
    SGRStatement emphasis = null;
    /**
     * BLINK_SLOW, BLINK_RAPID, BLINK_NORMAL
     */
    SGRStatement blink = null;
    /**
     * FONT_0, FONT_1, FONT_2, FONT_3, FONT_4, FONT_5, FONT_6, FONT_7, FONT_8, FONT_9
     */
    SGRStatement font = null;
    /**
     * CONCEAL
     */
    SGRStatement conceal = null;
    /**
     * PROP_SPACING_ON, PROP_SPACING_OFF
     */
    SGRStatement propSpacing = null;

    public TerminalState(@NotNull TerminalReference terminalAccessor) {
        this.terminal = terminalAccessor;
    }

    /**
     * Manually resets the active conditions
     */
    public void reset() {
        intensity = null;
        fgIntensity = null;
        bgIntensity = null;
        fgColor = null;
        bgColor = null;
        underline = null;
        underlineColor = null;
        strike = null;
        invert = null;
        emphasis = null;
        blink = null;
        font = null;
        conceal = null;
        propSpacing = null;
    }

    /**
     * Compresses a {@link SGRSequence}, removing overridden and unnecessary statements.
     *
     * @param sequence The sequence to compress
     */
    public void apply(@NotNull SGRSequence sequence) {
        apply(sequence.toSGRStatements());
    }

    /**
     * Compresses {@link SGRStatement}s, removing overridden and unnecessary statements.
     *
     * @param statements The statements to compress
     */
    public void apply(@NotNull List<SGRStatement> statements) {
        // Search for a reset marker
        int resetMarker = -1;
        for (SGRStatement statement : statements) {
            if (statement.getType() == SGRStatement.Type.RESET) {
                resetMarker = statements.indexOf(statement);
            }
        }

        // If there's a reset marker, then remove it and everything that comes before.
        if (resetMarker != -1) {
            statements = statements.subList(resetMarker + 1, statements.size());
            reset();
        }

        // Filter out unnecessary statements
        for (SGRStatement statement : statements) {
            apply(statement);
        }
    }

    /**
     * Compresses a {@link SGRStatement}, removing overridden and unnecessary statements.
     *
     * @param statement The statement to compress
     */
    public void apply(@NotNull SGRStatement statement) {
        switch (statement.getType()) {
            case RESET:
                reset();
                break;

            case INTENSITY_OFF:
            case INTENSITY_BRIGHT_OR_BOLD:
            case INTENSITY_DIM_OR_THIN:
                intensity = statement;
                break;

            case FG_BLK:
            case FG_RED:
            case FG_GRN:
            case FG_YEL:
            case FG_BLU:
            case FG_MAG:
            case FG_CYA:
            case FG_WHI:
                if (intensity != null) {
                    fgIntensity = intensity;
                    intensity = null;
                }
                fgColor = statement;
                break;

            case FG_RGB:
            case FG_DEFAULT:
                if (terminal.supportsIceColor()) intensity = null;

                // FIXME: 12/21/20 incomplete code
                int[] args = statement.getArguments();

                fgColor = statement;
                break;

            case FG_BLK_BRIGHT:
            case FG_RED_BRIGHT:
            case FG_GRN_BRIGHT:
            case FG_YEL_BRIGHT:
            case FG_BLU_BRIGHT:
            case FG_MAG_BRIGHT:
            case FG_CYA_BRIGHT:
            case FG_WHI_BRIGHT:
                if (terminal.supportsIceColor()) intensity = null;
                fgColor = statement;
                break;

            case BG_BLK:
            case BG_RED:
            case BG_GRN:
            case BG_YEL:
            case BG_BLU:
            case BG_MAG:
            case BG_CYA:
            case BG_WHI:
                if (intensity != null) {
                    bgIntensity = intensity;
                    intensity = null;
                }

                bgColor = statement;
                break;

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
                if (terminal.supportsIceColor()) intensity = null;

                bgColor = statement;
                break;

            case UNDERLINE_OFF:
            case UNDERLINE_SINGLE:
            case UNDERLINE_DOUBLE:
                underline = statement;
                break;

            case UNDERLINE_COLOR:
            case UNDERLINE_COLOR_DEFAULT:
                underlineColor = statement;
                break;

            case STRIKE_ON:
            case STRIKE_OFF:
                strike = statement;
                break;

            case INVERT_ON:
            case INVERT_OFF:
                invert = statement;
                break;

            case EMPHASIS_OFF:
            case EMPHASIS_ITALIC:
            case EMPHASIS_FRAKTUR:
                emphasis = statement;
                break;

            case BLINK_OFF:
            case BLINK_SLOW:
            case BLINK_FAST:
                blink = statement;
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

            case CONCEAL_ON:
            case CONCEAL_OFF:
                conceal = statement;
                break;

            case PROP_SPACING_ON:
            case PROP_SPACING_OFF:
                propSpacing = statement;
                break;
        }
    }

    /**
     * @return a compressed list of {@link SGRStatement}s
     */
    public @NotNull List<@NotNull SGRStatement> getStatements() {
        List<SGRStatement> statements = new ArrayList<>();

        if (intensity != null) statements.add(intensity);
        if (fgIntensity != null) statements.add(fgIntensity);
        if (bgIntensity != null) statements.add(bgIntensity);

        if (fgColor != null) statements.add(fgColor);
        if (bgColor != null) statements.add(bgColor);

        if (underline != null) statements.add(underline);
        if (underlineColor != null) statements.add(underlineColor);

        if (strike != null) statements.add(strike);
        if (invert != null) statements.add(invert);
        if (emphasis != null) statements.add(emphasis);
        if (blink != null) statements.add(blink);
        if (font != null) statements.add(font);
        if (conceal != null) statements.add(conceal);
        if (propSpacing != null) statements.add(propSpacing);

        return statements;
    }

    public @Nullable List<@NotNull SGRStatement> cancel(@NotNull List<@NotNull SGRStatement> statements) {
        TerminalState newTerminalState = new TerminalState(terminal);
        newTerminalState.apply(statements);

        if (this.equals(newTerminalState)) {
            return null;
        } else {
            // FIXME: 12/19/20 this is stupid
            //statements.add(0, new SGRStatement(SGRStatement.Type.RESET));
            return new ArrayList<>(statements) {{
                add(0, new SGRStatement(SGRStatement.Type.RESET));
            }};
        }
    }

    public @Nullable List<@NotNull SGRStatement> cancelAndApply(@NotNull List<@NotNull SGRStatement> statements) {
        List<SGRStatement> newStatements = cancel(statements);
        if (newStatements != null) apply(newStatements);
        return newStatements;
    }

    @Override
    public @NotNull String toString() {
        return super.toString() + "\n" + propertiesToString();
    }

    public @NotNull String propertiesToString() {
        StringBuilder sb = new StringBuilder("[");

        sb.append("intensity=").append(intensity).append(",");
        sb.append("fgIntensity=").append(fgIntensity).append(",");
        sb.append("bgIntensity=").append(bgIntensity).append(",\n");
        sb.append("fgColor=").append(fgColor).append(",");
        sb.append("bgColor=").append(bgColor).append(",");
        sb.append("underline=").append(underline).append(",\n");
        sb.append("underlineColor=").append(underlineColor).append(",");
        sb.append("strike=").append(strike).append(",");
        sb.append("invert=").append(invert).append(",\n");
        sb.append("emphasis=").append(emphasis).append(",");
        sb.append("blink=").append(blink).append(",");
        sb.append("font=").append(font).append(",\n");
        sb.append("conceal=").append(conceal).append(",");
        sb.append("propSpacing=").append(propSpacing).append(",");

        sb.deleteCharAt(sb.length() - 1).append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TerminalState) {
            TerminalState terminalState = (TerminalState) obj;
            return this.propertiesToString().equals(terminalState.propertiesToString())
                    && this.terminal.equals(terminalState.terminal);
        } else {
            return false;
        }
    }
}
