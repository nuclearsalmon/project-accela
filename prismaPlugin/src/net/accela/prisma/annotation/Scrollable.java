package net.accela.prisma.annotation;

import java.io.IOException;

/**
 * Describes an area that can be 'scrolled', by moving a range of lines up or down.
 * Certain terminals will implement this through extensions and are much faster than
 * trying to manually erase and re-print the text.
 */
public interface Scrollable {
    /**
     * Scroll a range of lines of this {@link Scrollable} according to given distance.
     * <p>
     * If scroll-range is empty (firstLine &gt; lastLine || distance == 0) then
     * this method does nothing.
     * <p>
     * Lines that are scrolled away from are cleared.
     * <p>
     * If absolute value of distance is equal or greater than number of lines
     * in range, then all lines within the range will be cleared.
     *
     * @param firstLine first line of the range to be scrolled (top line is 0)
     * @param lastLine  last (inclusive) line of the range to be scrolled
     * @param distance  if &gt; 0: move lines up, else if &lt; 0: move lines down.
     * @throws IOException If there was an I/O error when running the operation
     */
    void scrollLines(int firstLine, int lastLine, int distance) throws IOException;
}
