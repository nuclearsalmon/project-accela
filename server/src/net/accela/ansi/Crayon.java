package net.accela.ansi;

import net.accela.ansi.annotation.NotWidelySupported;
import net.accela.ansi.exception.ESCSequenceException;
import net.accela.ansi.sequence.ESCSequence;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.SGRStatement;
import net.accela.ansi.sequence.color.StandardColor;
import net.accela.ansi.sequence.color.TableColor;
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

    //
    // EFFECTS
    //

    /**
     * Resets ALL attributes before this.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon reset() {
        statements.add(new SGRStatement(SGRStatement.Type.RESET));
        return this;
    }

    // Intensity
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
        statements.add(new SGRStatement(SGRStatement.Type.INTENSITY_OFF));
        return this;
    }

    // Text styles and fonts
    /**
     * Enables italic style. Not widely supported. Sometimes treated as an inverse or blink effect.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    @NotWidelySupported
    public @NotNull Crayon italicStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.EMPHASIS_ITALIC));
        return this;
    }

    /**
     * Enables fraktur style. Rarely supported.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    @NotWidelySupported
    public @NotNull Crayon frakturStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.EMPHASIS_FRAKTUR));
        return this;
    }

    /**
     * Disables italic and fraktur styles.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon normalStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.EMPHASIS_OFF));
        return this;
    }

    /**
     * Changes the font.
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

    // Underline and strikethrough
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
        statements.add(new SGRStatement(SGRStatement.Type.UNDERLINE_OFF));
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

    // Cursor blinking
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
        statements.add(new SGRStatement(SGRStatement.Type.BLINK_OFF));
        return this;
    }

    // Color inversion
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

    // Text concealing
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

    //
    // Colors
    //

    // Black
    /**
     * Enables black foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blackFg() {
        return black(true, false);
    }

    /**
     * Enables black foreground color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blackFg(boolean bright) {
        return black(true, bright);
    }

    /**
     * Enables black background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blackBg() {
        return black(false, false);
    }

    /**
     * Enables black background color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blackBg(boolean bright) {
        return black(false, bright);
    }

    /**
     * Enables black color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon black(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.BLK, fg, bright).toSGRStatements());
        return this;
    }

    // Red color
    /**
     * Enables red foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon redFg() {
        return red(true, false);
    }

    /**
     * Enables red foreground color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon redFg(boolean bright) {
        return red(true, bright);
    }

    /**
     * Enables red background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon redBg() {
        return red(false, false);
    }

    /**
     * Enables red background color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon redFb(boolean bright) {
        return red(false, bright);
    }

    /**
     * Enables black color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon red(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.RED, fg, bright).toSGRStatements());
        return this;
    }

    // Green color
    /**
     * Enables green foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon greenFg() {
        return green(true, false);
    }

    /**
     * Enables green foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon greenFg(boolean bright) {
        return green(true, bright);
    }

    /**
     * Enables green background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon greenBg() {
        return green(false, false);
    }

    /**
     * Enables green background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon greenBg(boolean bright) {
        return green(false, bright);
    }

    /**
     * Enables green color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon green(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.GRN, fg, bright).toSGRStatements());
        return this;
    }

    // Yellow color
    /**
     * Enables yellow foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon yellowFg() {
        return yellow(true, false);
    }

    /**
     * Enables yellow foreground color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon yellowFg(boolean bright) {
        return yellow(true, bright);
    }

    /**
     * Enables yellow background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon yellowBg() {
        return yellow(false, false);
    }

    /**
     * Enables yellow background color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon yellowBg(boolean bright) {
        return yellow(false, bright);
    }

    /**
     * Enables yellow color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon yellow(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.YEL, fg, bright).toSGRStatements());
        return this;
    }

    // Blue color
    /**
     * Enables blue foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blueFg() {
        return blue(true, false);
    }

    /**
     * Enables blue foreground color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blueFg(boolean bright) {
        return blue(true, bright);
    }

    /**
     * Enables blue background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blueBg() {
        return blue(false, false);
    }

    /**
     * Enables blue background color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blueBg(boolean bright) {
        return blue(false, bright);
    }

    /**
     * Enables blue color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon blue(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.BLU, fg, bright).toSGRStatements());
        return this;
    }

    // Magenta color
    /**
     * Enables magenta foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon magentaFg() {
        return magenta(true, false);
    }

    /**
     * Enables magenta foreground color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon magentaFg(boolean bright) {
        return magenta(true, bright);
    }

    /**
     * Enables magenta background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon magentaBg() {
        return magenta(false, false);
    }

    /**
     * Enables magenta background color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon magentaBg(boolean bright) {
        return magenta(false, bright);
    }

    /**
     * Enables magenta color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon magenta(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.MAG, fg, bright).toSGRStatements());
        return this;
    }

    // Cyan color
    /**
     * Enables cyan foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon cyanFg() {
        return cyan(true, false);
    }

    /**
     * Enables cyan foreground color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon cyanFg(boolean bright) {
        return cyan(true, bright);
    }

    /**
     * Enables cyan background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon cyanBg() {
        return cyan(false, false);
    }

    /**
     * Enables cyan background color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon cyanBg(boolean bright) {
        return cyan(false, bright);
    }

    /**
     * Enables cyan color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon cyan(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.CYA, fg, bright).toSGRStatements());
        return this;
    }

    // White color
    /**
     * Enables white foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon whiteFg() {
        return white(true, false);
    }

    /**
     * Enables white foreground color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon whiteFg(boolean bright) {
        return white(true, bright);
    }

    /**
     * Enables white background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon whiteBg() {
        return white(false, false);
    }

    /**
     * Enables white background color.
     *
     * @param bright Whether to use a bright color or not
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon whiteBg(boolean bright) {
        return white(false, bright);
    }

    /**
     * Enables white color.
     *
     * @param fg     Whether to use a foreground color or a background color.
     * @param bright Whether to use a bright color or not.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon white(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.WHI, fg, bright).toSGRStatements());
        return this;
    }

    // RGB color
    /**
     * Enables a foreground RGB color.
     *
     * @param r The amount of red to use
     * @param g The amount of green to use
     * @param b The amount of blue to use
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon rgbFg(int r, int g, int b) {
        statements.addAll(new TrueColor(new RGB(r, g, b), true).toSGRStatements());
        return this;
    }

    /**
     * Enables a foreground RGB color.
     *
     * @param rgb The RGB values to use.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon rgbFg(@NotNull RGB rgb) {
        statements.addAll(new TrueColor(rgb, true).toSGRStatements());
        return this;
    }

    /**
     * Enables a background RGB color.
     *
     * @param r The amount of red to use
     * @param g The amount of green to use
     * @param b The amount of blue to use
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon rgbBg(int r, int g, int b) {
        statements.addAll(new TrueColor(new RGB(r, g, b), false).toSGRStatements());
        return this;
    }

    /**
     * Enables a background RGB color.
     *
     * @param rgb The RGB values to use.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon rgbBg(@NotNull RGB rgb) {
        statements.addAll(new TrueColor(rgb, false).toSGRStatements());
        return this;
    }

    // RGB Table color
    /**
     * Enables a foreground RGB color from a 8-bit color table.
     *
     * @param index Color index in the table.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon tableFg(@Range(from = 0, to = 255) int index) {
        statements.addAll(new TableColor(index, true).toSGRStatements());
        return this;
    }

    /**
     * Enables a background RGB color from a 8-bit color table.
     *
     * @param index Color index in the table.
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon tableBg(@Range(from = 0, to = 255) int index) {
        statements.addAll(new TableColor(index, false).toSGRStatements());
        return this;
    }

    // Default color
    /**
     * Enables the default foreground color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon defaultFg() {
        statements.add(new SGRStatement(SGRStatement.Type.FG_DEFAULT));
        return this;
    }

    /**
     * Enables the default background color.
     *
     * @return The {@link Crayon} instance this was executed on.
     */
    public @NotNull Crayon defaultBg() {
        statements.add(new SGRStatement(SGRStatement.Type.BG_DEFAULT));
        return this;
    }
}
