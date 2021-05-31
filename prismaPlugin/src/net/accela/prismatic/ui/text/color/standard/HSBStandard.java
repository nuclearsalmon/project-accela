package net.accela.prismatic.ui.text.color.standard;

import org.jetbrains.annotations.NotNull;

public class HSBStandard {
    final float hue, saturation, brightness;

    public HSBStandard(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public static @NotNull HSBStandard fromRGB(@NotNull RGBStandard rgb) {
        int r = rgb.getRed();
        int g = rgb.getGreen();
        int b = rgb.getBlue();

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

        return new HSBStandard(hue, saturation, brightness);
    }
}
