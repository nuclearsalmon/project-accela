package net.accela.ansi.sequence.color;

import net.accela.ansi.exception.ESCSequenceException;
import net.accela.ansi.sequence.CSISequence;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.color.standard.RGB;
import org.jetbrains.annotations.NotNull;

public class TrueColor extends SGRSequence {
    final @NotNull RGB rgb;
    final boolean fg;

    public TrueColor(int r, int g, int b) throws ESCSequenceException {
        this(new RGB(r, g, b), true);
    }

    public TrueColor(int r, int g, int b, boolean fg) throws ESCSequenceException {
        this(new RGB(r, g, b), fg);
    }

    public TrueColor(@NotNull RGB rgb) throws ESCSequenceException {
        this(rgb, true);
    }

    public TrueColor(@NotNull RGB rgb, boolean fg) throws ESCSequenceException {
        this.rgb = rgb;
        this.fg = fg;
        this.sequenceString = CSISequence.CSI_STRING + (fg ? "3" : "4") + "8;2;" + rgb.r + ";" + rgb.g + ";" + rgb.b + "m";
    }

    public boolean isFG() {
        return fg;
    }

    public @NotNull RGB getRGB() {
        return rgb;
    }
}
