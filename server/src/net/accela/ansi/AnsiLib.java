package net.accela.ansi;

/**
 * @deprecated AnsiLib provides shorthand methods and variables so that the programmer
 * does not need to manually type "\u001B[" etc each time they need a sequence.
 * It will probably get removed in the future.
 * <br>
 * For more information regarding the ANSI Specification, refer to:
 * https://en.wikipedia.org/wiki/ANSI_escape_code
 */
@Deprecated
public class AnsiLib {
    /**
     * Escape key sequence initializer ("\u001b" or "\27")
     */
    @SuppressWarnings("unused")
    public static final String ESC = "\u001B";

    /**
     * CSI - Control Sequence Inducer
     */
    @SuppressWarnings("unused")
    public static final String CSI = ESC + "[";

    /**
     * String Terminator
     */
    @SuppressWarnings("unused")
    public static final String ST = ESC + "\\";

    /**
     * Reset to Initial State
     */
    @SuppressWarnings("unused")
    public static final String RIS = ESC + "c";

    /**
     * CUP - Cursor Position
     * Moves the cursor to x, y. Lowest is 1, not 0.
     *
     * @param x Column
     * @param y Row
     */
    @SuppressWarnings("UnusedReturnValue")
    public static String CUP(int x, int y) {
        return CSI + y + ";" + x + "H";
    }

    /**
     * Clears the screen.
     */
    public static String CLR = CSI + "2J";

    /**
     * Shows the cursor
     */
    public static String showCursor = CSI + "?25h";

    /**
     * Hides the cursor
     */
    @SuppressWarnings("UnusedReturnValue")
    public static String hideCursor = CSI + "?25l";
}
