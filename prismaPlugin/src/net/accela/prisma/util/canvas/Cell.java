package net.accela.prisma.util.canvas;

import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.util.WCWidth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * A {@link Cell} in the terminal.
 * <br><br>
 * This is designed to be immutable.
 * Whilst I get that's a pain in the ass and a half, it makes programming this shit a heck of a lot easier for me.
 * I might replace this with something more efficient later in a couple of months or so...
 * if I'm feeling like over-engineering something for no reason. We'll see. Well.. back to work.<br>
 * - me, to myself in a couple of months from the time of writing...
 */
public class Cell {
    private final String codepoint;
    private final SGRSequence sequence;
    private final @Range(from = -1, to = 2) int characterWidth;

    //
    // Constructors
    //

    /**
     * Creates a {@link Cell}.
     */
    public Cell() {
        this.codepoint = null;
        this.sequence = null;
        this.characterWidth = 0;
    }

    /**
     * Creates a {@link Cell}.
     *
     * @param codepoint Must contain only a single codepoint or null.
     * @param sequence  Any SGRSequence or null.
     */
    public Cell(@Nullable String codepoint, @Nullable SGRSequence sequence) {
        // Checks
        if (codepoint != null && codepoint.codePointCount(0, codepoint.length()) > 1) {
            throw new IllegalArgumentException("The String must contain only a single CodePoint");
        }

        this.codepoint = codepoint;
        this.sequence = sequence;
        // todo test if this actually works
        this.characterWidth = codepoint == null || codepoint.codePointCount(0, codepoint.length()) <= 0
                ? 0 : WCWidth.wcwidth(codepoint.codePointAt(0));
    }

    //
    // Getters
    //

    /**
     * @return This {@link Cell}s codepoint, if any.
     */
    public @Nullable String getCodepoint() {
        return codepoint;
    }

    /**
     * @return This {@link Cell}s {@link SGRSequence}, if any.
     */
    public @Nullable SGRSequence getSequence() {
        return sequence;
    }

    //
    // Calculations
    //

    /**
     * @return See {@link WCWidth#wcwidth(int)} for more info on what these numbers mean.
     * <ul>
     *     <li>"-1" : Indeterminate (not printable or C0/C1 control characters).</li>
     *     <li>"0" : Does not advance the cursor, such as NULL or Combining.</li>
     *     <li>"1" : All others.</li>
     *     <li>"2" : Characters of category East Asian Wide (W) or East Asian Full-width (F)
     *         which are displayed using two terminal cells.</li>
     * </ul>
     * @see WCWidth#wcwidth(int)
     */
    public @Range(from = -1, to = 2) int getCharacterWidth() {
        return characterWidth;
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cell)) {
            return false;
        } else {
            Cell cell = (Cell) obj;
            return this.codepoint.equals(cell.codepoint) && this.sequence.equals(cell.sequence);
        }
    }

    @Override
    @NotNull
    public String toString() {
        return super.toString() + "[codePoint=" + codepoint + ",sequence=" + sequence + "]";
    }
}