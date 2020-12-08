package net.accela.ansi;

import net.accela.ansi.sequence.CSISequence;

/**
 * @deprecated AnsiLib provides shorthand methods and variables so that the programmer
 * does not need to manually type "\u001B[" etc each time they need a sequence.
 * It will get removed (replaced with a better system) eventually.
 */
@Deprecated
public class AnsiLib {
    /**
     * CUP - Cursor Position
     * Moves the cursor to x, y. Lowest is 1, not 0.
     *
     * @param x Column
     * @param y Row
     */
    public static String CUP(int x, int y) {
        return CSISequence.CSI_STRING + y + ";" + x + "H";
    }
}
