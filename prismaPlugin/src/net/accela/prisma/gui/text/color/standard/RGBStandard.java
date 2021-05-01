package net.accela.prisma.gui.text.color.standard;

import org.jetbrains.annotations.NotNull;

public class RGBStandard {
    public final int r;
    public final int g;
    public final int b;

    public RGBStandard(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IndexOutOfBoundsException("The int values needs to be between 0 and 255");
        }

        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public float getDifference(@NotNull RGBStandard rgbToCompareAgainst) {
        return getDifference(this, rgbToCompareAgainst);
    }

    public static float getDifference(@NotNull RGBStandard rgb1, @NotNull RGBStandard rgb2) {
        return (float) Math.sqrt(
                Math.pow((rgb2.r - rgb1.r) * 0.3, 2)
                        + Math.pow((rgb2.g - rgb1.g) * 0.59, 2)
                        + Math.pow((rgb2.b - rgb1.b) * 0.11, 2)
        );
    }

    public static @NotNull RGBStandard fromHSB(@NotNull HSBStandard hsb) {
        float hue = hsb.getHue();
        float saturation = hsb.getSaturation();
        float brightness = hsb.getBrightness();

        int r = 0;
        int g = 0;
        int b = 0;

        if (saturation == 0.0F) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0F;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - saturation * (1.0F - f));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (t * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 1:
                    r = (int) (q * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 2:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (t * 255.0F + 0.5F);
                    break;
                case 3:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (q * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 4:
                    r = (int) (t * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 5:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (q * 255.0F + 0.5F);
            }
        }

        return new RGBStandard(r, g, b);
    }
}
