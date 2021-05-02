package net.accela.prismatic.gui.text.color;

import net.accela.prismatic.gui.text.color.standard.RGBStandard;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * This is based on Lanterna's TextColor (by Martin Berglund).
 * <p>
 * If my original color system was about as intelligent as a small mossy pebble,
 * then Lanterna's TextColor has the intelligence of a whole fucking alien civilization.
 * <p>
 * Yes, I'm rewriting my entire textgfx sgr system, of course I'm annoyed.
 */
public interface TextColor {
    /**
     * Returns the byte sgr in between CSI and character 'm' that is used to enable this color as the foreground
     * color on an ANSI-compatible lowlevel.
     *
     * @return Byte array out data to output in between of CSI and 'm'
     */
    byte[] getForegroundSGRBytes();

    /**
     * Returns the byte sgr in between CSI and character 'm' that is used to enable this color as the background
     * color on an ANSI-compatible lowlevel.
     *
     * @return Byte array out data to output in between of CSI and 'm'
     */
    byte[] getBackgroundSGRBytes();

    /**
     * @return Red intensity of this color, from 0 to 255
     */
    int getRed();

    /**
     * @return Green intensity of this color, from 0 to 255
     */
    int getGreen();

    /**
     * @return Blue intensity of this color, from 0 to 255
     */
    int getBlue();

    /**
     * @return RGBStandard representation of this color
     */
    @NotNull RGBStandard getRBG();

    /**
     * This class represent classic ANSI colors that are likely to be very compatible with most lowlevel
     * implementations. It is limited to 8 colors (plus the 'default' color) but as a norm, using bold mode (SGR code)
     * will slightly alter the color, giving it a bit brighter tone, so in total this will give you 16 (+1) colors.
     * <p>
     * For more information, see http://en.wikipedia.org/wiki/File:Ansi.png
     */
    enum ANSI implements TextColor {
        BLACK(0, 0, 0, 0),
        RED(1, 170, 0, 0),
        GREEN(2, 0, 170, 0),
        YELLOW(3, 170, 85, 0),
        BLUE(4, 0, 0, 170),
        MAGENTA(5, 170, 0, 170),
        CYAN(6, 0, 170, 170),
        WHITE(7, 170, 170, 170),
        DEFAULT(9, 0, 0, 0),
        BLACK_BRIGHT(0, true, 85, 85, 85),
        RED_BRIGHT(1, true, 255, 85, 85),
        GREEN_BRIGHT(2, true, 85, 255, 85),
        YELLOW_BRIGHT(3, true, 255, 255, 85),
        BLUE_BRIGHT(4, true, 85, 85, 255),
        MAGENTA_BRIGHT(5, true, 255, 85, 255),
        CYAN_BRIGHT(6, true, 85, 255, 255),
        WHITE_BRIGHT(7, true, 255, 255, 255);

        private final boolean bright;
        private final int red;
        private final int green;
        private final int blue;
        private final byte[] foregroundSGR;
        private final byte[] backgroundSGR;

        ANSI(int index, int red, int green, int blue) {
            this(index, false, red, green, blue);
        }

        ANSI(int index, boolean bright, int red, int green, int blue) {
            this.bright = bright;
            this.red = red;
            this.green = green;
            this.blue = blue;
            foregroundSGR = String.format("%d%d", bright ? 9 : 3, index).getBytes();
            backgroundSGR = String.format("%d%d", bright ? 10 : 4, index).getBytes();
        }

        @Override
        public byte[] getForegroundSGRBytes() {
            return foregroundSGR.clone();
        }

        @Override
        public byte[] getBackgroundSGRBytes() {
            return backgroundSGR.clone();
        }

        public boolean isBright() {
            return bright;
        }

        @Override
        public int getRed() {
            return red;
        }

        @Override
        public int getGreen() {
            return green;
        }

        @Override
        public int getBlue() {
            return blue;
        }

        /**
         * @return RGBStandard representation of this color
         */
        @Override
        public @NotNull RGBStandard getRBG() {
            return new RGBStandard(red, green, blue);
        }

        /**
         * Will approximate to the closest ANSI color
         *
         * @param rgb The rgb value to try and approximate
         * @return The closest ANSI color match
         */
        public static @NotNull ANSI fromRGB(@NotNull RGBStandard rgb) {
            ANSI closestANSI = ANSI.BLACK;
            float closestDifference = Float.MAX_VALUE;

            for (ANSI ansi : ANSI.values()) {
                RGBStandard comparisonRGB = ansi.getRBG();

                float difference = RGBStandard.getDifference(rgb, comparisonRGB);
                if (difference < closestDifference) {
                    closestDifference = difference;
                    closestANSI = ansi;
                }
            }

            return closestANSI;
        }
    }

    /**
     * This class represents a color expressed in the indexed XTerm 256 color extension, where each color is defined in a
     * lookup-table. All in all, there are 256 codes, but in order to know which one to know you either need to have the
     * table at hand, or you can use the two static helper methods which can help you convert from three 8-bit
     * RGBStandard values to the closest approximate indexed color number. If you are interested, the 256 index values are
     * actually divided like this:<br>
     * 0 .. 15 - System colors, same as ANSI, but the actual rendered color depends on the lowlevel emulators color scheme<br>
     * 16 .. 231 - Forms a 6x6x6 RGBStandard color cube<br>
     * 232 .. 255 - A gray scale ramp (without black and white endpoints)<br>
     * <p>
     * Support for indexed colors is somewhat widely adopted, not as much as the ANSI colors (TextColor.ANSI) but more
     * than the RGBStandard (TextColor.RGBStandard).
     * <p>
     * For more details on this, please see <a
     * href="https://github.com/robertknight/konsole/blob/master/user-doc/README.moreColors">
     * this</a> commit message to Konsole.
     */
    class Indexed implements TextColor {
        private static final byte[][] COLOR_TABLE = new byte[][]{
                //These are the standard 16-color VGA palette entries
                {(byte) 0, (byte) 0, (byte) 0},
                {(byte) 170, (byte) 0, (byte) 0},
                {(byte) 0, (byte) 170, (byte) 0},
                {(byte) 170, (byte) 85, (byte) 0},
                {(byte) 0, (byte) 0, (byte) 170},
                {(byte) 170, (byte) 0, (byte) 170},
                {(byte) 0, (byte) 170, (byte) 170},
                {(byte) 170, (byte) 170, (byte) 170},
                {(byte) 85, (byte) 85, (byte) 85},
                {(byte) 255, (byte) 85, (byte) 85},
                {(byte) 85, (byte) 255, (byte) 85},
                {(byte) 255, (byte) 255, (byte) 85},
                {(byte) 85, (byte) 85, (byte) 255},
                {(byte) 255, (byte) 85, (byte) 255},
                {(byte) 85, (byte) 255, (byte) 255},
                {(byte) 255, (byte) 255, (byte) 255},

                //Starting 6x6x6 RGBStandard color cube from 16
                {(byte) 0x00, (byte) 0x00, (byte) 0x00},
                {(byte) 0x00, (byte) 0x00, (byte) 0x5f},
                {(byte) 0x00, (byte) 0x00, (byte) 0x87},
                {(byte) 0x00, (byte) 0x00, (byte) 0xaf},
                {(byte) 0x00, (byte) 0x00, (byte) 0xd7},
                {(byte) 0x00, (byte) 0x00, (byte) 0xff},
                {(byte) 0x00, (byte) 0x5f, (byte) 0x00},
                {(byte) 0x00, (byte) 0x5f, (byte) 0x5f},
                {(byte) 0x00, (byte) 0x5f, (byte) 0x87},
                {(byte) 0x00, (byte) 0x5f, (byte) 0xaf},
                {(byte) 0x00, (byte) 0x5f, (byte) 0xd7},
                {(byte) 0x00, (byte) 0x5f, (byte) 0xff},
                {(byte) 0x00, (byte) 0x87, (byte) 0x00},
                {(byte) 0x00, (byte) 0x87, (byte) 0x5f},
                {(byte) 0x00, (byte) 0x87, (byte) 0x87},
                {(byte) 0x00, (byte) 0x87, (byte) 0xaf},
                {(byte) 0x00, (byte) 0x87, (byte) 0xd7},
                {(byte) 0x00, (byte) 0x87, (byte) 0xff},
                {(byte) 0x00, (byte) 0xaf, (byte) 0x00},
                {(byte) 0x00, (byte) 0xaf, (byte) 0x5f},
                {(byte) 0x00, (byte) 0xaf, (byte) 0x87},
                {(byte) 0x00, (byte) 0xaf, (byte) 0xaf},
                {(byte) 0x00, (byte) 0xaf, (byte) 0xd7},
                {(byte) 0x00, (byte) 0xaf, (byte) 0xff},
                {(byte) 0x00, (byte) 0xd7, (byte) 0x00},
                {(byte) 0x00, (byte) 0xd7, (byte) 0x5f},
                {(byte) 0x00, (byte) 0xd7, (byte) 0x87},
                {(byte) 0x00, (byte) 0xd7, (byte) 0xaf},
                {(byte) 0x00, (byte) 0xd7, (byte) 0xd7},
                {(byte) 0x00, (byte) 0xd7, (byte) 0xff},
                {(byte) 0x00, (byte) 0xff, (byte) 0x00},
                {(byte) 0x00, (byte) 0xff, (byte) 0x5f},
                {(byte) 0x00, (byte) 0xff, (byte) 0x87},
                {(byte) 0x00, (byte) 0xff, (byte) 0xaf},
                {(byte) 0x00, (byte) 0xff, (byte) 0xd7},
                {(byte) 0x00, (byte) 0xff, (byte) 0xff},
                {(byte) 0x5f, (byte) 0x00, (byte) 0x00},
                {(byte) 0x5f, (byte) 0x00, (byte) 0x5f},
                {(byte) 0x5f, (byte) 0x00, (byte) 0x87},
                {(byte) 0x5f, (byte) 0x00, (byte) 0xaf},
                {(byte) 0x5f, (byte) 0x00, (byte) 0xd7},
                {(byte) 0x5f, (byte) 0x00, (byte) 0xff},
                {(byte) 0x5f, (byte) 0x5f, (byte) 0x00},
                {(byte) 0x5f, (byte) 0x5f, (byte) 0x5f},
                {(byte) 0x5f, (byte) 0x5f, (byte) 0x87},
                {(byte) 0x5f, (byte) 0x5f, (byte) 0xaf},
                {(byte) 0x5f, (byte) 0x5f, (byte) 0xd7},
                {(byte) 0x5f, (byte) 0x5f, (byte) 0xff},
                {(byte) 0x5f, (byte) 0x87, (byte) 0x00},
                {(byte) 0x5f, (byte) 0x87, (byte) 0x5f},
                {(byte) 0x5f, (byte) 0x87, (byte) 0x87},
                {(byte) 0x5f, (byte) 0x87, (byte) 0xaf},
                {(byte) 0x5f, (byte) 0x87, (byte) 0xd7},
                {(byte) 0x5f, (byte) 0x87, (byte) 0xff},
                {(byte) 0x5f, (byte) 0xaf, (byte) 0x00},
                {(byte) 0x5f, (byte) 0xaf, (byte) 0x5f},
                {(byte) 0x5f, (byte) 0xaf, (byte) 0x87},
                {(byte) 0x5f, (byte) 0xaf, (byte) 0xaf},
                {(byte) 0x5f, (byte) 0xaf, (byte) 0xd7},
                {(byte) 0x5f, (byte) 0xaf, (byte) 0xff},
                {(byte) 0x5f, (byte) 0xd7, (byte) 0x00},
                {(byte) 0x5f, (byte) 0xd7, (byte) 0x5f},
                {(byte) 0x5f, (byte) 0xd7, (byte) 0x87},
                {(byte) 0x5f, (byte) 0xd7, (byte) 0xaf},
                {(byte) 0x5f, (byte) 0xd7, (byte) 0xd7},
                {(byte) 0x5f, (byte) 0xd7, (byte) 0xff},
                {(byte) 0x5f, (byte) 0xff, (byte) 0x00},
                {(byte) 0x5f, (byte) 0xff, (byte) 0x5f},
                {(byte) 0x5f, (byte) 0xff, (byte) 0x87},
                {(byte) 0x5f, (byte) 0xff, (byte) 0xaf},
                {(byte) 0x5f, (byte) 0xff, (byte) 0xd7},
                {(byte) 0x5f, (byte) 0xff, (byte) 0xff},
                {(byte) 0x87, (byte) 0x00, (byte) 0x00},
                {(byte) 0x87, (byte) 0x00, (byte) 0x5f},
                {(byte) 0x87, (byte) 0x00, (byte) 0x87},
                {(byte) 0x87, (byte) 0x00, (byte) 0xaf},
                {(byte) 0x87, (byte) 0x00, (byte) 0xd7},
                {(byte) 0x87, (byte) 0x00, (byte) 0xff},
                {(byte) 0x87, (byte) 0x5f, (byte) 0x00},
                {(byte) 0x87, (byte) 0x5f, (byte) 0x5f},
                {(byte) 0x87, (byte) 0x5f, (byte) 0x87},
                {(byte) 0x87, (byte) 0x5f, (byte) 0xaf},
                {(byte) 0x87, (byte) 0x5f, (byte) 0xd7},
                {(byte) 0x87, (byte) 0x5f, (byte) 0xff},
                {(byte) 0x87, (byte) 0x87, (byte) 0x00},
                {(byte) 0x87, (byte) 0x87, (byte) 0x5f},
                {(byte) 0x87, (byte) 0x87, (byte) 0x87},
                {(byte) 0x87, (byte) 0x87, (byte) 0xaf},
                {(byte) 0x87, (byte) 0x87, (byte) 0xd7},
                {(byte) 0x87, (byte) 0x87, (byte) 0xff},
                {(byte) 0x87, (byte) 0xaf, (byte) 0x00},
                {(byte) 0x87, (byte) 0xaf, (byte) 0x5f},
                {(byte) 0x87, (byte) 0xaf, (byte) 0x87},
                {(byte) 0x87, (byte) 0xaf, (byte) 0xaf},
                {(byte) 0x87, (byte) 0xaf, (byte) 0xd7},
                {(byte) 0x87, (byte) 0xaf, (byte) 0xff},
                {(byte) 0x87, (byte) 0xd7, (byte) 0x00},
                {(byte) 0x87, (byte) 0xd7, (byte) 0x5f},
                {(byte) 0x87, (byte) 0xd7, (byte) 0x87},
                {(byte) 0x87, (byte) 0xd7, (byte) 0xaf},
                {(byte) 0x87, (byte) 0xd7, (byte) 0xd7},
                {(byte) 0x87, (byte) 0xd7, (byte) 0xff},
                {(byte) 0x87, (byte) 0xff, (byte) 0x00},
                {(byte) 0x87, (byte) 0xff, (byte) 0x5f},
                {(byte) 0x87, (byte) 0xff, (byte) 0x87},
                {(byte) 0x87, (byte) 0xff, (byte) 0xaf},
                {(byte) 0x87, (byte) 0xff, (byte) 0xd7},
                {(byte) 0x87, (byte) 0xff, (byte) 0xff},
                {(byte) 0xaf, (byte) 0x00, (byte) 0x00},
                {(byte) 0xaf, (byte) 0x00, (byte) 0x5f},
                {(byte) 0xaf, (byte) 0x00, (byte) 0x87},
                {(byte) 0xaf, (byte) 0x00, (byte) 0xaf},
                {(byte) 0xaf, (byte) 0x00, (byte) 0xd7},
                {(byte) 0xaf, (byte) 0x00, (byte) 0xff},
                {(byte) 0xaf, (byte) 0x5f, (byte) 0x00},
                {(byte) 0xaf, (byte) 0x5f, (byte) 0x5f},
                {(byte) 0xaf, (byte) 0x5f, (byte) 0x87},
                {(byte) 0xaf, (byte) 0x5f, (byte) 0xaf},
                {(byte) 0xaf, (byte) 0x5f, (byte) 0xd7},
                {(byte) 0xaf, (byte) 0x5f, (byte) 0xff},
                {(byte) 0xaf, (byte) 0x87, (byte) 0x00},
                {(byte) 0xaf, (byte) 0x87, (byte) 0x5f},
                {(byte) 0xaf, (byte) 0x87, (byte) 0x87},
                {(byte) 0xaf, (byte) 0x87, (byte) 0xaf},
                {(byte) 0xaf, (byte) 0x87, (byte) 0xd7},
                {(byte) 0xaf, (byte) 0x87, (byte) 0xff},
                {(byte) 0xaf, (byte) 0xaf, (byte) 0x00},
                {(byte) 0xaf, (byte) 0xaf, (byte) 0x5f},
                {(byte) 0xaf, (byte) 0xaf, (byte) 0x87},
                {(byte) 0xaf, (byte) 0xaf, (byte) 0xaf},
                {(byte) 0xaf, (byte) 0xaf, (byte) 0xd7},
                {(byte) 0xaf, (byte) 0xaf, (byte) 0xff},
                {(byte) 0xaf, (byte) 0xd7, (byte) 0x00},
                {(byte) 0xaf, (byte) 0xd7, (byte) 0x5f},
                {(byte) 0xaf, (byte) 0xd7, (byte) 0x87},
                {(byte) 0xaf, (byte) 0xd7, (byte) 0xaf},
                {(byte) 0xaf, (byte) 0xd7, (byte) 0xd7},
                {(byte) 0xaf, (byte) 0xd7, (byte) 0xff},
                {(byte) 0xaf, (byte) 0xff, (byte) 0x00},
                {(byte) 0xaf, (byte) 0xff, (byte) 0x5f},
                {(byte) 0xaf, (byte) 0xff, (byte) 0x87},
                {(byte) 0xaf, (byte) 0xff, (byte) 0xaf},
                {(byte) 0xaf, (byte) 0xff, (byte) 0xd7},
                {(byte) 0xaf, (byte) 0xff, (byte) 0xff},
                {(byte) 0xd7, (byte) 0x00, (byte) 0x00},
                {(byte) 0xd7, (byte) 0x00, (byte) 0x5f},
                {(byte) 0xd7, (byte) 0x00, (byte) 0x87},
                {(byte) 0xd7, (byte) 0x00, (byte) 0xaf},
                {(byte) 0xd7, (byte) 0x00, (byte) 0xd7},
                {(byte) 0xd7, (byte) 0x00, (byte) 0xff},
                {(byte) 0xd7, (byte) 0x5f, (byte) 0x00},
                {(byte) 0xd7, (byte) 0x5f, (byte) 0x5f},
                {(byte) 0xd7, (byte) 0x5f, (byte) 0x87},
                {(byte) 0xd7, (byte) 0x5f, (byte) 0xaf},
                {(byte) 0xd7, (byte) 0x5f, (byte) 0xd7},
                {(byte) 0xd7, (byte) 0x5f, (byte) 0xff},
                {(byte) 0xd7, (byte) 0x87, (byte) 0x00},
                {(byte) 0xd7, (byte) 0x87, (byte) 0x5f},
                {(byte) 0xd7, (byte) 0x87, (byte) 0x87},
                {(byte) 0xd7, (byte) 0x87, (byte) 0xaf},
                {(byte) 0xd7, (byte) 0x87, (byte) 0xd7},
                {(byte) 0xd7, (byte) 0x87, (byte) 0xff},
                {(byte) 0xd7, (byte) 0xaf, (byte) 0x00},
                {(byte) 0xd7, (byte) 0xaf, (byte) 0x5f},
                {(byte) 0xd7, (byte) 0xaf, (byte) 0x87},
                {(byte) 0xd7, (byte) 0xaf, (byte) 0xaf},
                {(byte) 0xd7, (byte) 0xaf, (byte) 0xd7},
                {(byte) 0xd7, (byte) 0xaf, (byte) 0xff},
                {(byte) 0xd7, (byte) 0xd7, (byte) 0x00},
                {(byte) 0xd7, (byte) 0xd7, (byte) 0x5f},
                {(byte) 0xd7, (byte) 0xd7, (byte) 0x87},
                {(byte) 0xd7, (byte) 0xd7, (byte) 0xaf},
                {(byte) 0xd7, (byte) 0xd7, (byte) 0xd7},
                {(byte) 0xd7, (byte) 0xd7, (byte) 0xff},
                {(byte) 0xd7, (byte) 0xff, (byte) 0x00},
                {(byte) 0xd7, (byte) 0xff, (byte) 0x5f},
                {(byte) 0xd7, (byte) 0xff, (byte) 0x87},
                {(byte) 0xd7, (byte) 0xff, (byte) 0xaf},
                {(byte) 0xd7, (byte) 0xff, (byte) 0xd7},
                {(byte) 0xd7, (byte) 0xff, (byte) 0xff},
                {(byte) 0xff, (byte) 0x00, (byte) 0x00},
                {(byte) 0xff, (byte) 0x00, (byte) 0x5f},
                {(byte) 0xff, (byte) 0x00, (byte) 0x87},
                {(byte) 0xff, (byte) 0x00, (byte) 0xaf},
                {(byte) 0xff, (byte) 0x00, (byte) 0xd7},
                {(byte) 0xff, (byte) 0x00, (byte) 0xff},
                {(byte) 0xff, (byte) 0x5f, (byte) 0x00},
                {(byte) 0xff, (byte) 0x5f, (byte) 0x5f},
                {(byte) 0xff, (byte) 0x5f, (byte) 0x87},
                {(byte) 0xff, (byte) 0x5f, (byte) 0xaf},
                {(byte) 0xff, (byte) 0x5f, (byte) 0xd7},
                {(byte) 0xff, (byte) 0x5f, (byte) 0xff},
                {(byte) 0xff, (byte) 0x87, (byte) 0x00},
                {(byte) 0xff, (byte) 0x87, (byte) 0x5f},
                {(byte) 0xff, (byte) 0x87, (byte) 0x87},
                {(byte) 0xff, (byte) 0x87, (byte) 0xaf},
                {(byte) 0xff, (byte) 0x87, (byte) 0xd7},
                {(byte) 0xff, (byte) 0x87, (byte) 0xff},
                {(byte) 0xff, (byte) 0xaf, (byte) 0x00},
                {(byte) 0xff, (byte) 0xaf, (byte) 0x5f},
                {(byte) 0xff, (byte) 0xaf, (byte) 0x87},
                {(byte) 0xff, (byte) 0xaf, (byte) 0xaf},
                {(byte) 0xff, (byte) 0xaf, (byte) 0xd7},
                {(byte) 0xff, (byte) 0xaf, (byte) 0xff},
                {(byte) 0xff, (byte) 0xd7, (byte) 0x00},
                {(byte) 0xff, (byte) 0xd7, (byte) 0x5f},
                {(byte) 0xff, (byte) 0xd7, (byte) 0x87},
                {(byte) 0xff, (byte) 0xd7, (byte) 0xaf},
                {(byte) 0xff, (byte) 0xd7, (byte) 0xd7},
                {(byte) 0xff, (byte) 0xd7, (byte) 0xff},
                {(byte) 0xff, (byte) 0xff, (byte) 0x00},
                {(byte) 0xff, (byte) 0xff, (byte) 0x5f},
                {(byte) 0xff, (byte) 0xff, (byte) 0x87},
                {(byte) 0xff, (byte) 0xff, (byte) 0xaf},
                {(byte) 0xff, (byte) 0xff, (byte) 0xd7},
                {(byte) 0xff, (byte) 0xff, (byte) 0xff},

                //Grey-scale ramp from 232
                {(byte) 0x08, (byte) 0x08, (byte) 0x08},
                {(byte) 0x12, (byte) 0x12, (byte) 0x12},
                {(byte) 0x1c, (byte) 0x1c, (byte) 0x1c},
                {(byte) 0x26, (byte) 0x26, (byte) 0x26},
                {(byte) 0x30, (byte) 0x30, (byte) 0x30},
                {(byte) 0x3a, (byte) 0x3a, (byte) 0x3a},
                {(byte) 0x44, (byte) 0x44, (byte) 0x44},
                {(byte) 0x4e, (byte) 0x4e, (byte) 0x4e},
                {(byte) 0x58, (byte) 0x58, (byte) 0x58},
                {(byte) 0x62, (byte) 0x62, (byte) 0x62},
                {(byte) 0x6c, (byte) 0x6c, (byte) 0x6c},
                {(byte) 0x76, (byte) 0x76, (byte) 0x76},
                {(byte) 0x80, (byte) 0x80, (byte) 0x80},
                {(byte) 0x8a, (byte) 0x8a, (byte) 0x8a},
                {(byte) 0x94, (byte) 0x94, (byte) 0x94},
                {(byte) 0x9e, (byte) 0x9e, (byte) 0x9e},
                {(byte) 0xa8, (byte) 0xa8, (byte) 0xa8},
                {(byte) 0xb2, (byte) 0xb2, (byte) 0xb2},
                {(byte) 0xbc, (byte) 0xbc, (byte) 0xbc},
                {(byte) 0xc6, (byte) 0xc6, (byte) 0xc6},
                {(byte) 0xd0, (byte) 0xd0, (byte) 0xd0},
                {(byte) 0xda, (byte) 0xda, (byte) 0xda},
                {(byte) 0xe4, (byte) 0xe4, (byte) 0xe4},
                {(byte) 0xee, (byte) 0xee, (byte) 0xee}
        };

        private final int colorIndex;

        /**
         * Creates a new TextColor using the XTerm 256 color indexed mode, with the specified index value. You must
         * choose a value between 0 and 255.
         *
         * @param colorIndex Index value to use for this color.
         */
        public Indexed(int colorIndex) {
            if (colorIndex > 255 || colorIndex < 0) {
                throw new IllegalArgumentException("Cannot create a Color.Indexed with a color index of " + colorIndex +
                        ", must be in the range of 0-255");
            }
            this.colorIndex = colorIndex;
        }

        @Override
        public byte[] getForegroundSGRBytes() {
            return ("38;5;" + colorIndex).getBytes();
        }

        @Override
        public byte[] getBackgroundSGRBytes() {
            return ("48;5;" + colorIndex).getBytes();
        }

        @Override
        public int getRed() {
            return COLOR_TABLE[colorIndex][0] & 0x000000ff;
        }

        @Override
        public int getGreen() {
            return COLOR_TABLE[colorIndex][1] & 0x000000ff;
        }

        @Override
        public int getBlue() {
            return COLOR_TABLE[colorIndex][2] & 0x000000ff;
        }

        /**
         * @return RGBStandard representation of this color
         */
        @Override
        public @NotNull RGBStandard getRBG() {
            return new RGBStandard(getRed(), getGreen(), getBlue());
        }

        @Override
        public String toString() {
            return "{IndexedColor:" + colorIndex + "}";
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 43 * hash + this.colorIndex;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Indexed other = (Indexed) obj;
            return this.colorIndex == other.colorIndex;
        }

        /**
         * Picks out a color approximated from the supplied RGBStandard components
         *
         * @param red   Red intensity, from 0 to 255
         * @param green Red intensity, from 0 to 255
         * @param blue  Red intensity, from 0 to 255
         * @return Nearest color from the 6x6x6 RGBStandard color cube or from the 24 entries grey-scale ramp (whichever is closest)
         */
        public static Indexed fromRGB(int red, int green, int blue) {
            if (red < 0 || red > 255) {
                throw new IllegalArgumentException("fromRGB: red is outside of valid range (0-255)");
            }
            if (green < 0 || green > 255) {
                throw new IllegalArgumentException("fromRGB: green is outside of valid range (0-255)");
            }
            if (blue < 0 || blue > 255) {
                throw new IllegalArgumentException("fromRGB: blue is outside of valid range (0-255)");
            }

            int rescaledRed = (int) (((double) red / 255.0) * 5.0);
            int rescaledGreen = (int) (((double) green / 255.0) * 5.0);
            int rescaledBlue = (int) (((double) blue / 255.0) * 5.0);

            int index = rescaledBlue + (6 * rescaledGreen) + (36 * rescaledRed) + 16;
            Indexed fromColorCube = new Indexed(index);
            Indexed fromGreyRamp = fromGreyRamp((red + green + blue) / 3);

            //Now figure out which one is closest
            int coloredDistance = ((red - fromColorCube.getRed()) * (red - fromColorCube.getRed())) +
                    ((green - fromColorCube.getGreen()) * (green - fromColorCube.getGreen())) +
                    ((blue - fromColorCube.getBlue()) * (blue - fromColorCube.getBlue()));
            int greyDistance = ((red - fromGreyRamp.getRed()) * (red - fromGreyRamp.getRed())) +
                    ((green - fromGreyRamp.getGreen()) * (green - fromGreyRamp.getGreen())) +
                    ((blue - fromGreyRamp.getBlue()) * (blue - fromGreyRamp.getBlue()));
            if (coloredDistance < greyDistance) {
                return fromColorCube;
            } else {
                return fromGreyRamp;
            }
        }

        /**
         * Picks out a color from the grey-scale ramp area of the color index.
         *
         * @param intensity Intensity, 0 - 255
         * @return Indexed color from the grey-scale ramp which is the best match for the supplied intensity
         */
        private static Indexed fromGreyRamp(int intensity) {
            int rescaled = (int) (((double) intensity / 255.0) * 23.0) + 232;
            return new Indexed(rescaled);
        }
    }

    /**
     * This class can be used to specify a color in 24-bit color space (RGBStandard with 8-bit resolution per color). Please be
     * aware that only a few lowlevel support 24-bit color control codes, please avoid using this class unless you know
     * all users will have compatible terminals. For details, please see
     * <a href="https://github.com/robertknight/konsole/blob/master/user-doc/README.moreColors">
     * this</a> commit log. Behavior on terminals that don't support these codes is undefined.
     */
    class RGB implements TextColor {
        private final int red;
        private final int green;
        private final int blue;

        /**
         * This class can be used to specify a color in 24-bit color space (RGBStandard with 8-bit resolution per color). Please be
         * aware that only a few lowlevel support 24-bit color control codes, please avoid using this class unless you know
         * all users will have compatible terminals. For details, please see
         * <a href="https://github.com/robertknight/konsole/blob/master/user-doc/README.moreColors">
         * this</a> commit log. Behavior on terminals that don't support these codes is undefined.
         *
         * @param r Red intensity, from 0 to 255
         * @param g Green intensity, from 0 to 255
         * @param b Blue intensity, from 0 to 255
         */
        public RGB(int r, int g, int b) {
            if (r < 0 || r > 255) {
                throw new IllegalArgumentException("RGBStandard: r is outside of valid range (0-255)");
            }
            if (g < 0 || g > 255) {
                throw new IllegalArgumentException("RGBStandard: g is outside of valid range (0-255)");
            }
            if (b < 0 || b > 255) {
                throw new IllegalArgumentException("RGBStandard: b is outside of valid range (0-255)");
            }
            this.red = r;
            this.green = g;
            this.blue = b;
        }

        @Override
        public byte[] getForegroundSGRBytes() {
            return ("38;2;" + getRed() + ";" + getGreen() + ";" + getBlue()).getBytes();
        }

        @Override
        public byte[] getBackgroundSGRBytes() {
            return ("48;2;" + getRed() + ";" + getGreen() + ";" + getBlue()).getBytes();
        }

        @Override
        public int getRed() {
            return red;
        }

        @Override
        public int getGreen() {
            return green;
        }

        @Override
        public int getBlue() {
            return blue;
        }

        /**
         * @return RGBStandard representation of this color
         */
        @Override
        public @NotNull RGBStandard getRBG() {
            return new RGBStandard(red, blue, green);
        }

        @Override
        public String toString() {
            return "{RGBStandard:" + getRed() + "," + getGreen() + "," + getBlue() + "}";
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + red;
            hash = 29 * hash + green;
            hash = 29 * hash + blue;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RGB other = (RGB) obj;
            return this.red == other.red
                    && this.green == other.green
                    && this.blue == other.blue;
        }
    }

    /**
     * Utility class to instantiate colors from other types and definitions
     */
    class Factory {
        private static final Pattern INDEXED_COLOR = Pattern.compile("#[0-9]{1,3}");
        private static final Pattern RGB_COLOR = Pattern.compile("#[0-9a-fA-F]{6}");

        private Factory() {
        }

        /**
         * Parses a string into a color. The string can have one of three formats:
         * <ul>
         *     <li><i>blue</i> - Constant value from the {@link ANSI} enum</li>
         *     <li><i>#17</i> - Hash character followed by one to three numbers; picks the color with that index from
         *     the 256 color palette</li>
         *     <li><i>#1a1a1a</i> - Hash character followed by three hex-decimal tuples; creates an RGBStandard color entry by
         *     parsing the tuples as Red, Green and Blue</li>
         * </ul>
         *
         * @param value The string value to parse
         * @return A {@link TextColor} that is either an {@link ANSI}, an {@link Indexed} or an {@link RGB} depending on
         * the format of the string, or {@code null} if {@code value} is {@code null}.
         */
        public static TextColor fromString(String value) {
            if (value == null) {
                return null;
            }
            value = value.trim();
            if (RGB_COLOR.matcher(value).matches()) {
                int r = Integer.parseInt(value.substring(1, 3), 16);
                int g = Integer.parseInt(value.substring(3, 5), 16);
                int b = Integer.parseInt(value.substring(5, 7), 16);
                return new TextColor.RGB(r, g, b);
            } else if (INDEXED_COLOR.matcher(value).matches()) {
                int index = Integer.parseInt(value.substring(1));
                return new TextColor.Indexed(index);
            }
            try {
                return TextColor.ANSI.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown color definition \"" + value + "\"", e);
            }
        }
    }
}
