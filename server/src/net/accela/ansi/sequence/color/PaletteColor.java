package net.accela.ansi.sequence.color;

import net.accela.ansi.AnsiLib;
import net.accela.ansi.exception.ESCSequenceException;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.ansi.sequence.color.standard.RGB;
import org.jetbrains.annotations.NotNull;

public class PaletteColor extends SGRSequence {
    int index;
    boolean fg;

    public PaletteColor(int index) throws ESCSequenceException {
        this(index, true);
    }

    public PaletteColor(int index, boolean fg) throws ESCSequenceException {
        if (index < 0 | index > 255) {
            throw new IndexOutOfBoundsException("The int value needs to be between 0 and 255");
        }
        this.index = index;
        this.fg = fg;
        this.sequenceString = AnsiLib.CSI + (fg ? "3" : "4") + "8;5;" + index + "m";
    }

    public PaletteColor(RGB rgb) throws ESCSequenceException {
        this(rgb, true);
    }

    public PaletteColor(RGB rgb, boolean fg) throws ESCSequenceException {
        this.fg = fg;
        // We use the extended greyscale palette here, with the exception of
        // black and white. Normal palette only has 4 greyscale shades.
        if (rgb.r == rgb.g && rgb.g == rgb.b) {
            if (rgb.r < 8) {
                index = 16;
            } else if (rgb.r > 248) {
                index = 231;
            } else {
                index = Math.round((((float) rgb.r - 8) / 247) * 24) + 232;
            }
        }
        // It's not grayscale, so approximate it instead
        else {
            index = 16
                    + (36 * Math.round((float) rgb.r / 255 * 5))
                    + (6 * Math.round((float) rgb.g / 255 * 5))
                    + Math.round((float) rgb.b / 255 * 5);
        }

        // Prefer 4B color encoding when possible
        if (index < 16) {
            this.sequenceString = new StandardColor(index).toString();
        } else {
            this.sequenceString = AnsiLib.CSI + (fg ? "3" : "4") + "8;5;" + index + "m";
        }
    }

    public boolean isFG() {
        return fg;
    }

    public int getIndex() {
        return index;
    }

    public int getIndexR() {
        return (index - 16) / 36;
    }

    public int getIndexG() {
        return ((index - 16) % 36) / 6;
    }

    public int getIndexB() {
        return (index - 16) % 6;
    }

    @NotNull
    public RGB toRGB() {
        // Colors 0 - 15 are normal 4-bit colors
        if (index < 16) {
            return new StandardColor(index).toRGB();
        }
        // Colors 16 - 231 are a 6x6 color cube
        else if (index < 232) {
            int indexR = getIndexR(), indexG = getIndexG(), indexB = getIndexB();

            int rRGB = 55 + indexR * 40;
            int gRGB = 55 + indexG * 40;
            int bRGB = 55 + indexB * 40;

            return new RGB(rRGB, gRGB, bRGB);
        }
        // Colors 232 - 255 are a grayscale ramp, intentionally leaving out black and white
        else {
            int all = (index - 232) * 10 + 8;
            return new RGB(all, all, all);
        }
    }
}
