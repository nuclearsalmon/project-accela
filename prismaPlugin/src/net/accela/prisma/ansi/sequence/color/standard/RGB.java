package net.accela.prisma.ansi.sequence.color.standard;

import org.jetbrains.annotations.NotNull;

public class RGB {
    public final int r;
    public final int g;
    public final int b;

    public RGB(int r, int g, int b) {
        if (r < 0 | r > 255 | g < 0 | g > 255 | b < 0 | b > 255) {
            throw new IndexOutOfBoundsException("The int values needs to be between 0 and 255");
        }

        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public HSB toHSB() {
        int cmax = Math.max(r, g);
        if (b > cmax) cmax = b;

        int cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        float brightness = (float) cmax / 255.0F;
        float saturation;
        if (cmax != 0) {
            saturation = (float) (cmax - cmin) / (float) cmax;
        } else {
            saturation = 0.0F;
        }

        float hue;
        if (saturation == 0.0F) {
            hue = 0.0F;
        } else {
            float red = (float) (cmax - r) / (float) (cmax - cmin);
            float green = (float) (cmax - g) / (float) (cmax - cmin);
            float blue = (float) (cmax - b) / (float) (cmax - cmin);
            if (r == cmax) {
                hue = blue - green;
            } else if (g == cmax) {
                hue = 2.0F + red - blue;
            } else {
                hue = 4.0F + green - red;
            }

            hue /= 6.0F;
            if (hue < 0.0F) {
                ++hue;
            }
        }

        return new HSB(hue, saturation, brightness);
    }

    public float getDifference(@NotNull RGB rgbToCompareAgainst) {
        return getDifference(this, rgbToCompareAgainst);
    }

    public static float getDifference(@NotNull RGB rgb1, @NotNull RGB rgb2) {
        return (float) Math.sqrt(
                Math.pow((rgb2.r - rgb1.r) * 0.3, 2)
                        + Math.pow((rgb2.g - rgb1.g) * 0.59, 2)
                        + Math.pow((rgb2.b - rgb1.b) * 0.11, 2)
        );
    }
}
