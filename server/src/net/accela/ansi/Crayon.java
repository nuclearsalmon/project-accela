package net.accela.ansi;

import net.accela.ansi.annotation.NotWidelySupported;
import net.accela.ansi.exception.ESCSequenceException;
import net.accela.ansi.sequence.ESCSequence;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.SGRStatement;
import net.accela.ansi.sequence.color.PaletteColor;
import net.accela.ansi.sequence.color.StandardColor;
import net.accela.ansi.sequence.color.TrueColor;
import net.accela.ansi.sequence.color.standard.RGB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Crayon} is a high level tool that builds upon the {@link SGRSequence} framework.
 * They are inter-compatible, and the programmer may use whichever one she or he prefers;
 * the functionality provided is more or less the same. {@link Crayon} is simply meant to shorter and easier to use,
 * not requiring any in-depth knowledge on how ANSI Escape Sequences work.
 * <br>
 * Another noteworthy difference is that {@link Crayon} is mutable,
 * whereas {@link ESCSequence} objects are immutable.
 */
// TODO: 11/16/20 Fully document this one.
public class Crayon extends SGRSequence {
    @NotNull List<@NotNull SGRStatement> statements = new ArrayList<>();

    ///
    /// MISC
    ///

    /**
     * @return The {@link SGRStatement}s that this {@link Crayon} consists of.
     */
    @Override
    public @NotNull List<@NotNull SGRStatement> toSGRStatements() {
        return List.copyOf(statements);
    }

    /**
     * Converts this {@link Crayon} into an {@link SGRSequence} and returns the String version of it.
     *
     * @return The {@link String} representation of this {@link Crayon}.
     */
    @Override
    public @NotNull String toString() {
        try {
            return new SGRSequence(statements).toString();
        } catch (ESCSequenceException ex) {
            System.out.println(ex.getSequenceAsString());
            throw ex;
        }
    }

    /**
     * @return The length (in characters) of the {@link String} representation of this {@link Crayon}.
     * @see CharSequence
     */
    @Override
    public int length() {
        return toString().length();
    }

    /**
     * @param i The index get the character from.
     * @return A single character from the {@link String} representation of this {@link Crayon}, at the requested index.
     * @see CharSequence
     */
    @Override
    public char charAt(int i) {
        return toString().charAt(i);
    }

    /**
     * @param start The start index.
     * @param end   The end index.
     * @return A subsequence of the {@link String} representation of this {@link Crayon},
     * starting and ending at the provided indexes.
     * @see CharSequence
     */
    @Override
    public @NotNull CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    ///
    /// EFFECTS
    ///

    /**
     * Resets ALL attributes before this.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon reset() {
        statements.add(new SGRStatement(SGRStatement.Type.RESET));
        return this;
    }

    /// Intensity

    /**
     * Enables bright color intensity OR bold typefaces (this can vary between terminals).
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon brightIntensity() {
        statements.add(new SGRStatement(SGRStatement.Type.INTENSITY_BRIGHT_OR_BOLD));
        return this;
    }

    /**
     * Enables dim color intensity OR faint typefaces (this can vary between terminals).
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon dimIntensity() {
        statements.add(new SGRStatement(SGRStatement.Type.INTENSITY_DIM_OR_THIN));
        return this;
    }

    /**
     * Enables default color intensity. Neither bright or dim color intensity.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon normalIntensity() {
        statements.add(new SGRStatement(SGRStatement.Type.INTENSITY_DEFAULT));
        return this;
    }

    /// Text styles and fonts

    /**
     * Enables italic style. Not widely supported. Sometimes treated as an inverse or blink effect.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    @NotWidelySupported
    public @NotNull Crayon italicStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.STYLE_ITALIC));
        return this;
    }

    /**
     * Enables fraktur style. Rarely supported.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    @NotWidelySupported
    public @NotNull Crayon frakturStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.STYLE_FRAKTUR));
        return this;
    }

    /**
     * Disables italic and fraktur styles.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon normalStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.STYLE_DEFAULT));
        return this;
    }

    /**
     * Enables a different font.
     *
     * @param font Ranges from 0 - 9, 0 being the default font and the others being alternatives.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon font(@Range(from = 0, to = 9) int font) {
        SGRStatement.Type fontType;
        switch (font) {
            case 1:
                fontType = SGRStatement.Type.FONT_1;
                break;
            case 2:
                fontType = SGRStatement.Type.FONT_2;
                break;
            case 3:
                fontType = SGRStatement.Type.FONT_3;
                break;
            case 4:
                fontType = SGRStatement.Type.FONT_4;
                break;
            case 5:
                fontType = SGRStatement.Type.FONT_5;
                break;
            case 6:
                fontType = SGRStatement.Type.FONT_6;
                break;
            case 7:
                fontType = SGRStatement.Type.FONT_7;
                break;
            case 8:
                fontType = SGRStatement.Type.FONT_8;
                break;
            case 9:
                fontType = SGRStatement.Type.FONT_9;
                break;
            case 0:
            default:
                fontType = SGRStatement.Type.FONT_DEFAULT;
                break;
        }
        statements.add(new SGRStatement(fontType));
        return this;
    }

    /// Underline and strikethrough

    /**
     * Enables a single underline. Underline style extensions exists for some terminals.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon singleUnderline() {
        statements.add(new SGRStatement(SGRStatement.Type.UNDERLINE_SINGLE));
        return this;
    }

    /**
     * Enables a double underline OR disables bold text. Underline style extensions exists for some terminals.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon doubleUnderline() {
        statements.add(new SGRStatement(SGRStatement.Type.UNDERLINE_DOUBLE));
        return this;
    }

    /**
     * Disables underlines.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon noUnderline() {
        statements.add(new SGRStatement(SGRStatement.Type.UNDERLINE_NONE));
        return this;
    }

    /**
     * Enables a strike-through line across text. Also known as "Crossed out".
     * Makes characters legible, but marked as if for deletion.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon strike() {
        statements.add(new SGRStatement(SGRStatement.Type.STRIKE_ON));
        return this;
    }

    /**
     * Disables strike-through lines across text.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon noStrike() {
        statements.add(new SGRStatement(SGRStatement.Type.STRIKE_OFF));
        return this;
    }

    /// Blink

    /**
     * Enables slow cursor blinking. Less than 150 blinks per minute, according to the spec.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon slowBlink() {
        statements.add(new SGRStatement(SGRStatement.Type.BLINK_SLOW));
        return this;
    }

    /**
     * Enables fast cursor blinking. More than 150 blinks per minute, according to the spec. Not widely supported.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    @NotWidelySupported
    public @NotNull Crayon fastBlink() {
        statements.add(new SGRStatement(SGRStatement.Type.BLINK_FAST));
        return this;
    }

    /**
     * Disables cursor blinking.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon noBlink() {
        statements.add(new SGRStatement(SGRStatement.Type.BLINK_DEFAULT));
        return this;
    }

    /// Color Inversion

    /**
     * Enables color inversion by swapping the foreground and background colors.
     * Also known as "Reverse Video". Inconsistent emulation.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon invert() {
        statements.add(new SGRStatement(SGRStatement.Type.INVERT_ON));
        return this;
    }

    /**
     * Disables the color inversion effect; disables swapping foreground and background colors.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon noInvert() {
        statements.add(new SGRStatement(SGRStatement.Type.INVERT_OFF));
        return this;
    }

    /// Text concealing

    /**
     * Enables text concealing.
     * Also known as "Hide". Not widely supported.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    @NotWidelySupported
    public @NotNull Crayon conceal() {
        statements.add(new SGRStatement(SGRStatement.Type.CONCEAL_ON));
        return this;
    }

    /**
     * Disables text concealing.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    @NotWidelySupported
    public @NotNull Crayon noConceal() {
        statements.add(new SGRStatement(SGRStatement.Type.CONCEAL_OFF));
        return this;
    }

    /// Colors

    public @NotNull Crayon black() {
        return black(true, false);
    }

    public @NotNull Crayon black(boolean fg) {
        return black(fg, false);
    }

    public @NotNull Crayon black(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.BLK, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon red() {
        return red(true, false);
    }

    public @NotNull Crayon red(boolean fg) {
        return red(fg, false);
    }

    public @NotNull Crayon red(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.RED, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon green() {
        return green(true, false);
    }

    public @NotNull Crayon green(boolean fg) {
        return green(fg, false);
    }

    public @NotNull Crayon green(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.GRN, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon yellow() {
        return yellow(true, false);
    }

    public @NotNull Crayon yellow(boolean fg) {
        return yellow(fg, false);
    }

    public @NotNull Crayon yellow(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.YEL, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon blue() {
        return blue(true, false);
    }

    public @NotNull Crayon blue(boolean fg) {
        return blue(fg, false);
    }

    public @NotNull Crayon blue(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.BLU, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon magenta() {
        return magenta(true, false);
    }

    public @NotNull Crayon magenta(boolean fg) {
        return magenta(fg, false);
    }

    public @NotNull Crayon magenta(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.MAG, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon cyan() {
        return cyan(true, false);
    }

    public @NotNull Crayon cyan(boolean fg) {
        return cyan(fg, false);
    }

    public @NotNull Crayon cyan(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.CYA, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon white() {
        return white(true, false);
    }

    public @NotNull Crayon white(boolean fg) {
        return white(fg, false);
    }

    public @NotNull Crayon white(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.WHI, fg, bright).toSGRStatements());
        return this;
    }

    public @NotNull Crayon rgb(int r, int g, int b) {
        return rgb(new RGB(r, g, b));
    }

    public @NotNull Crayon rgb(int r, int g, int b, boolean fg) {
        return rgb(new RGB(r, g, b), fg);
    }

    public @NotNull Crayon rgb(@NotNull RGB rgb) {
        return rgb(rgb, true);
    }

    public @NotNull Crayon rgb(@NotNull RGB rgb, boolean fg) {
        statements.addAll(new TrueColor(rgb, fg).toSGRStatements());
        return this;
    }

    public @NotNull Crayon rgb(int index) {
        return rgb(index, true);
    }

    public @NotNull Crayon rgb(int index, boolean fg) {
        statements.addAll(new PaletteColor(index, fg).toSGRStatements());
        return this;
    }

    public @NotNull Crayon noColor() {
        return noColor(true);
    }

    public @NotNull Crayon noColor(boolean fg) {
        statements.add(new SGRStatement((fg ? SGRStatement.Type.FG_DEFAULT : SGRStatement.Type.BG_DEFAULT)));
        return this;
    }
}
