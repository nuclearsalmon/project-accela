package net.accela.prisma.ansi.sequence.color;

import net.accela.prisma.ansi.sequence.CSISequence;
import net.accela.prisma.ansi.sequence.ESCSequenceException;
import net.accela.prisma.ansi.sequence.SGRSequence;
import net.accela.prisma.ansi.sequence.SGRStatement;
import net.accela.prisma.ansi.sequence.color.standard.RGB;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class StandardColor extends SGRSequence {
    final StandardColor.ColorName colorName;
    final boolean fg;
    final boolean bright;

    /**
     * @param type   The type must be a standard color. Bright colors are allowed,
     *               but the brightness is ignored and is instead controlled by the bright argument
     * @param bright Whether the color is bright or not. Overrides colors such as FG_BLK_BRIGHT.
     */
    public StandardColor(@NotNull SGRStatement.Type type, boolean bright) {
        switch (type) {
            case FG_BLK:
            case FG_BLK_BRIGHT:
                colorName = ColorName.BLK;
                fg = true;
                this.bright = bright;
                break;
            case FG_RED:
            case FG_RED_BRIGHT:
                colorName = ColorName.RED;
                fg = true;
                this.bright = bright;
                break;
            case FG_GRN:
            case FG_GRN_BRIGHT:
                colorName = ColorName.GRN;
                fg = true;
                this.bright = bright;
                break;
            case FG_YEL:
            case FG_YEL_BRIGHT:
                colorName = ColorName.YEL;
                fg = true;
                this.bright = bright;
                break;
            case FG_BLU:
            case FG_BLU_BRIGHT:
                colorName = ColorName.BLU;
                fg = true;
                this.bright = bright;
                break;
            case FG_MAG:
            case FG_MAG_BRIGHT:
                colorName = ColorName.MAG;
                fg = true;
                this.bright = bright;
                break;
            case FG_CYA:
            case FG_CYA_BRIGHT:
                colorName = ColorName.CYA;
                fg = true;
                this.bright = bright;
                break;
            case FG_WHI:
            case FG_WHI_BRIGHT:
                colorName = ColorName.WHI;
                fg = true;
                this.bright = bright;
                break;
            case BG_BLK:
            case BG_BLK_BRIGHT:
                colorName = ColorName.BLK;
                fg = false;
                this.bright = bright;
                break;
            case BG_RED:
            case BG_RED_BRIGHT:
                colorName = ColorName.RED;
                fg = false;
                this.bright = bright;
                break;
            case BG_GRN:
            case BG_GRN_BRIGHT:
                colorName = ColorName.GRN;
                fg = false;
                this.bright = bright;
                break;
            case BG_YEL:
            case BG_YEL_BRIGHT:
                colorName = ColorName.YEL;
                fg = false;
                this.bright = bright;
                break;
            case BG_BLU:
            case BG_BLU_BRIGHT:
                colorName = ColorName.BLU;
                fg = false;
                this.bright = bright;
                break;
            case BG_MAG:
            case BG_MAG_BRIGHT:
                colorName = ColorName.MAG;
                fg = false;
                this.bright = bright;
                break;
            case BG_CYA:
            case BG_CYA_BRIGHT:
                colorName = ColorName.CYA;
                fg = false;
                this.bright = bright;
                break;
            case BG_WHI:
            case BG_WHI_BRIGHT:
                colorName = ColorName.WHI;
                fg = false;
                this.bright = bright;
                break;
            default:
                throw new ESCSequenceException("Statement type " + type + "is not a standard color");
        }

        this.sequenceString = toString();
    }

    public StandardColor(int index) {
        this(index, true);
    }

    public StandardColor(int index, boolean fg) {
        if (index < 0 || index > 15) throw new IndexOutOfBoundsException("Index can only be within the range of 0-15");

        this.fg = fg;
        this.bright = index > 7;
        this.colorName = IntToNameMap.get(bright ? (index - 8) : (index));

        this.sequenceString = toString();
    }

    public StandardColor(@NotNull StandardColor.ColorName colorName) {
        this(colorName, true, false);
    }

    public StandardColor(@NotNull StandardColor.ColorName colorName, boolean fg) throws ESCSequenceException {
        this(colorName, fg, false);
    }

    public StandardColor(@NotNull StandardColor.ColorName colorName, boolean fg, boolean bright) throws ESCSequenceException {
        this.colorName = colorName;
        this.fg = fg;
        this.bright = bright;

        this.sequenceString = toString();
    }

    public StandardColor(@NotNull RGB rgb) {
        this(rgb, true);
    }

    public StandardColor(@NotNull RGB rgb, boolean fg) {
        this.fg = fg;

        RGB closestRGB = null;
        float closestDifference = Float.MAX_VALUE;

        for (RGB comparisonRGB : RGBToColorIntMap.keySet()) {
            float difference = RGB.getDifference(rgb, comparisonRGB);
            if (difference < closestDifference) {
                closestDifference = difference;
                closestRGB = comparisonRGB;
            }
        }
        int colorAsInt = RGBToColorIntMap.get(closestRGB);
        if (colorAsInt > 7) {
            colorAsInt = colorAsInt - 9;
            this.bright = true;
        } else {
            this.bright = false;
        }
        this.colorName = IntToNameMap.get(colorAsInt);
    }

    public boolean isFg() {
        return fg;
    }

    public boolean isBright() {
        return bright;
    }

    public ColorName getColorName() {
        return colorName;
    }

    @Override
    @NotNull
    public String toString() {
        // Default to true because it makes it a whole lot easier to parse later if needed
        return toString(true);
    }

    @NotNull
    public String toString(final boolean aixterm) {
        int index = NameToIntMap.get(colorName);
        StringBuilder sequence = new StringBuilder(CSISequence.CSI_STRING);
        if (bright) {
            if (aixterm) {
                sequence.append(fg ? "9" : "10");
            } else {
                sequence.append("1;").append(fg ? "3" : "4");
            }
        } else {
            sequence.append(fg ? "3" : "4");
        }
        sequence.append(index).append("m");

        this.sequenceString = sequence.toString();

        return this.sequenceString;
    }

    public RGB toRGB() {
        if (bright) {
            return BrightNameToRGBMap.get(colorName);
        } else {
            return DimNameToRGBMap.get(colorName);
        }
    }

    public enum ColorName {
        BLK, RED, GRN, YEL, BLU, MAG, CYA, WHI
    }

    public static final HashMap<@NotNull Integer, @NotNull ColorName> IntToNameMap = new HashMap<>() {{
        put(0, ColorName.BLK);
        put(1, ColorName.RED);
        put(2, ColorName.GRN);
        put(3, ColorName.YEL);
        put(4, ColorName.BLU);
        put(5, ColorName.MAG);
        put(6, ColorName.CYA);
        put(7, ColorName.WHI);
    }};

    public static final HashMap<@NotNull ColorName, @NotNull Integer> NameToIntMap = new HashMap<>() {{
        put(ColorName.BLK, 0);
        put(ColorName.RED, 1);
        put(ColorName.GRN, 2);
        put(ColorName.YEL, 3);
        put(ColorName.BLU, 4);
        put(ColorName.MAG, 5);
        put(ColorName.CYA, 6);
        put(ColorName.WHI, 7);
    }};

    // Dim
    public static final HashMap<@NotNull ColorName, @NotNull RGB> DimNameToRGBMap = new HashMap<>() {{
        put(ColorName.BLK, new RGB(0, 0, 0));
        put(ColorName.RED, new RGB(170, 0, 0));
        put(ColorName.GRN, new RGB(0, 170, 0));
        put(ColorName.YEL, new RGB(170, 85, 0));
        put(ColorName.BLU, new RGB(0, 0, 170));
        put(ColorName.MAG, new RGB(170, 0, 170));
        put(ColorName.CYA, new RGB(0, 170, 170));
        put(ColorName.WHI, new RGB(170, 170, 170));
    }};

    // Bright
    public static final HashMap<@NotNull ColorName, @NotNull RGB> BrightNameToRGBMap = new HashMap<>() {{
        put(ColorName.BLK, new RGB(85, 85, 85));
        put(ColorName.RED, new RGB(255, 85, 85));
        put(ColorName.GRN, new RGB(85, 255, 85));
        put(ColorName.YEL, new RGB(255, 255, 85));
        put(ColorName.BLU, new RGB(85, 85, 255));
        put(ColorName.MAG, new RGB(255, 85, 255));
        put(ColorName.CYA, new RGB(85, 255, 255));
        put(ColorName.WHI, new RGB(255, 255, 255));
    }};

    public static final HashMap<@NotNull RGB, @NotNull Integer> RGBToColorIntMap = new HashMap<>() {{
        put(DimNameToRGBMap.get(ColorName.BLK), 0);
        put(DimNameToRGBMap.get(ColorName.RED), 1);
        put(DimNameToRGBMap.get(ColorName.GRN), 2);
        put(DimNameToRGBMap.get(ColorName.YEL), 3);
        put(DimNameToRGBMap.get(ColorName.BLU), 4);
        put(DimNameToRGBMap.get(ColorName.MAG), 5);
        put(DimNameToRGBMap.get(ColorName.CYA), 6);
        put(DimNameToRGBMap.get(ColorName.WHI), 7);
        put(BrightNameToRGBMap.get(ColorName.BLK), 8);
        put(BrightNameToRGBMap.get(ColorName.RED), 9);
        put(BrightNameToRGBMap.get(ColorName.GRN), 10);
        put(BrightNameToRGBMap.get(ColorName.YEL), 11);
        put(BrightNameToRGBMap.get(ColorName.BLU), 12);
        put(BrightNameToRGBMap.get(ColorName.MAG), 13);
        put(BrightNameToRGBMap.get(ColorName.CYA), 14);
        put(BrightNameToRGBMap.get(ColorName.WHI), 15);
    }};
}
