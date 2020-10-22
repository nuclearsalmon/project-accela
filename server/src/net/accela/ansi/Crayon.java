package net.accela.ansi;

import net.accela.ansi.exception.ESCSequenceException;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.SGRStatement;
import net.accela.ansi.sequence.color.PaletteColor;
import net.accela.ansi.sequence.color.StandardColor;
import net.accela.ansi.sequence.color.TrueColor;
import net.accela.ansi.sequence.color.standard.RGB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Crayon} is a somewhat more high level API than the {@link SGRSequence} API it builds upon.
 * They are (somewhat) inter-compatible, and the programmer may use whichever one
 * she or he prefers. The functionality provided is more or less the same,
 * the syntax for {@link Crayon} is just shorter and easier to use.
 */
public class Crayon extends SGRSequence {
    List<SGRStatement> statements = new ArrayList<>();

    //
    // MISC
    //

    @Override
    public @NotNull List<SGRStatement> toSGRStatements() {
        return List.copyOf(statements);
    }

    @Override
    public @NotNull String toString() {
        try {
            return new SGRSequence(statements).toString();
        } catch (ESCSequenceException ex) {
            System.out.println(ex.getSequenceAsString());
            throw ex;
        }
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int i) {
        return toString().charAt(i);
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        return toString().subSequence(i, i1);
    }

    //
    // EFFECTS
    //

    public Crayon reset() {
        statements.add(new SGRStatement(SGRStatement.Type.RESET));
        return this;
    }

    public Crayon brightIntensity() {
        statements.add(new SGRStatement(SGRStatement.Type.INTENSITY_BRIGHT_OR_BOLD));
        return this;
    }

    public Crayon dimIntensity() {
        statements.add(new SGRStatement(SGRStatement.Type.INTENSITY_DIM_OR_THIN));
        return this;
    }

    public Crayon italicStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.STYLE_ITALIC));
        return this;
    }

    public Crayon singleUnderline() {
        statements.add(new SGRStatement(SGRStatement.Type.UNDERLINE_SINGLE));
        return this;
    }

    public Crayon invert() {
        statements.add(new SGRStatement(SGRStatement.Type.INVERT_ON));
        return this;
    }

    public Crayon hide() {
        statements.add(new SGRStatement(SGRStatement.Type.CONCEAL_ON));
        return this;
    }

    public Crayon strike() {
        statements.add(new SGRStatement(SGRStatement.Type.STRIKE_ON));
        return this;
    }

    public Crayon font(int font) {
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
                fontType = SGRStatement.Type.FONT_0;
                break;
        }
        statements.add(new SGRStatement(fontType));
        return this;
    }


    public Crayon frakturStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.STYLE_FRAKTUR));
        return this;
    }

    public Crayon doubleUnderline() {
        statements.add(new SGRStatement(SGRStatement.Type.UNDERLINE_DOUBLE));
        return this;
    }

    public Crayon normalIntensity() {
        statements.add(new SGRStatement(SGRStatement.Type.INTENSITY_NORMAL));
        return this;
    }

    public Crayon normalStyle() {
        statements.add(new SGRStatement(SGRStatement.Type.STYLE_NORMAL));
        return this;
    }

    public Crayon noUnderline() {
        statements.add(new SGRStatement(SGRStatement.Type.UNDERLINE_NONE));
        return this;
    }

    public Crayon noInvert() {
        statements.add(new SGRStatement(SGRStatement.Type.INVERT_OFF));
        return this;
    }

    public Crayon noConceal() {
        statements.add(new SGRStatement(SGRStatement.Type.CONCEAL_OFF));
        return this;
    }

    public Crayon noStrike() {
        statements.add(new SGRStatement(SGRStatement.Type.STRIKE_OFF));
        return this;
    }

    public Crayon black() {
        return black(true, false);
    }

    public Crayon black(boolean fg) {
        return black(fg, false);
    }

    public Crayon black(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.BLK, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon red() {
        return red(true, false);
    }

    public Crayon red(boolean fg) {
        return red(fg, false);
    }

    public Crayon red(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.RED, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon green() {
        return green(true, false);
    }

    public Crayon green(boolean fg) {
        return green(fg, false);
    }

    public Crayon green(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.GRN, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon yellow() {
        return yellow(true, false);
    }

    public Crayon yellow(boolean fg) {
        return yellow(fg, false);
    }

    public Crayon yellow(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.YEL, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon blue() {
        return blue(true, false);
    }

    public Crayon blue(boolean fg) {
        return blue(fg, false);
    }

    public Crayon blue(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.BLU, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon magenta() {
        return magenta(true, false);
    }

    public Crayon magenta(boolean fg) {
        return magenta(fg, false);
    }

    public Crayon magenta(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.MAG, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon cyan() {
        return cyan(true, false);
    }

    public Crayon cyan(boolean fg) {
        return cyan(fg, false);
    }

    public Crayon cyan(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.CYA, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon white() {
        return white(true, false);
    }

    public Crayon white(boolean fg) {
        return white(fg, false);
    }

    public Crayon white(boolean fg, boolean bright) {
        statements.addAll(new StandardColor(StandardColor.ColorName.WHI, fg, bright).toSGRStatements());
        return this;
    }

    public Crayon rgb(int r, int g, int b) {
        return rgb(new RGB(r, g, b));
    }

    public Crayon rgb(int r, int g, int b, boolean fg) {
        return rgb(new RGB(r, g, b), fg);
    }

    public Crayon rgb(@NotNull RGB rgb) {
        return rgb(rgb, true);
    }

    public Crayon rgb(@NotNull RGB rgb, boolean fg) {
        statements.addAll(new TrueColor(rgb, fg).toSGRStatements());
        return this;
    }

    public Crayon rgb(int index) {
        return rgb(index, true);
    }

    public Crayon rgb(int index, boolean fg) {
        statements.addAll(new PaletteColor(index, fg).toSGRStatements());
        return this;
    }

    public Crayon noColor() {
        return noColor(true);
    }

    public Crayon noColor(boolean fg) {
        statements.add(new SGRStatement((fg ? SGRStatement.Type.FG_DEF : SGRStatement.Type.BG_DEF)));
        return this;
    }
}
