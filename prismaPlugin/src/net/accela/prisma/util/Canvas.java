package net.accela.prisma.util;

import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a 2 dimensional grid.
 * Behaves as expected, with index 0 always being the beginning (top left corner to be precise!)
 */
public class Canvas {
    /**
     * Cells can either be empty or null if that positon on the grid isn't occupied
     */
    private List<@Nullable Cell> cells;
    /**
     * The Rect this grid represents
     */
    private Rect rect;

    public Canvas(@NotNull Rect rect) {
        this.rect = rect;
        clear();
    }

    public Canvas(@NotNull Size size) {
        this(new Rect(size));
    }

    /**
     * Clones a {@link Canvas}.
     *
     * @param drawGrid The {@link Canvas} to clone.
     */
    public Canvas(@NotNull Canvas drawGrid) {
        this.cells = drawGrid.cells;
        this.rect = drawGrid.rect;
    }

    //
    // Setters
    //

    /**
     * Puts an cell to the position idx
     *
     * @param cell       to be added
     * @param coordinate position to add cell to
     */
    public void set(@NotNull Point coordinate, @Nullable Cell cell) {
        set(getIndex(coordinate.getX(), coordinate.getY()), cell);
    }

    /**
     * Puts an cell to the position idx
     *
     * @param cell to be added
     * @param x    position x to add cell to
     * @param y    position y to add cell to
     */
    public void set(int x, int y, @Nullable Cell cell) {
        set(getIndex(x, y), cell);
    }

    /**
     * Puts an cell to the position idx
     *
     * @param cell to be added
     * @param idx  to add cell at
     */
    public synchronized void set(int idx, @Nullable Cell cell) {
        if (idx >= cells.size()) throw new IndexOutOfBoundsException();
        cells.set(idx, cell);
    }

    //
    // Getters
    //

    /**
     * @return A {@link Rect} representing this {@link Canvas}'s size and position data.
     */
    public @NotNull Rect getRect() {
        return rect;
    }

    /**
     * @return A reference to this {@link Canvas}'s internal list of {@link Cell}s.
     */
    public @NotNull List<@Nullable Cell> getCells() {
        return cells;
    }

    /**
     * @return A {@link List} of {@link Cell}s between two {@link Point}s.
     */
    public @NotNull List<@Nullable Cell> getCells(@NotNull Point start, @NotNull Point end) {
        return cells.subList(getIndex(start.getX(), start.getY()), getIndex(end.getX(), end.getY()));
    }

    /**
     * @return iterator for a grid
     */
    public @NotNull Iterator<@Nullable Cell> iterator() {
        return cells.iterator();
    }

    /**
     * Returns the cell at position (x,y).
     *
     * @return the cell at position (x,y)
     */
    public @Nullable Cell get(@NotNull Point coordinate) {
        return cells.get(getIndex(coordinate.getX(), coordinate.getY()));
    }

    /**
     * Returns the cell at position (x,y).
     *
     * @return the cell at position (x,y)
     */
    public @Nullable Cell get(int x, int y) {
        return cells.get(getIndex(x, y));
    }

    /**
     * Returns the cell at index idx.
     *
     * @return the cell at given index
     */
    public @Nullable Cell get(int idx) {
        return cells.get(idx);
    }

    // todo investigate whether we actually need both capacity and amount methods, or if one will do

    /**
     * @return The capacity; width * height, or how many cells this {@link Canvas} can hold.
     * @see #getCellAmount()
     */
    public int getCapacity() {
        return rect.getCapacity();
    }

    /**
     * @return number of cells in grid, including null cells.
     * @see #getCapacity()
     */
    public int getCellAmount() {
        return cells.size();
    }

    //
    // Index calculations
    //

    /**
     * Returns the x coordinate from the index.
     *
     * @return x coordinate of the index
     */
    public int getX(int index) {
        return index % rect.getWidth();
    }

    /**
     * Returns the y coordinate from the index.
     *
     * @return y coordinate of the index
     */
    public int getY(int index) {
        return index / rect.getWidth();
    }

    /**
     * Returns index of cell at {@link Point} (x,y).
     *
     * @return index of the coordinates
     */
    public int getIndex(@NotNull Point point) {
        return getIndex(point.getX(), point.getY());
    }

    /**
     * Returns index of cell at (x,y).
     *
     * @return index of the coordinates
     */
    public int getIndex(int x, int y) {
        // Get dimensions
        int width = rect.getWidth(), height = rect.getHeight();

        // Ensure the coordinate point is within bounds
        if (x >= width) {
            throw new IndexOutOfBoundsException(String.format("X (%s) is out of bounds (%s)", x, width));
        }
        if (y >= height) {
            throw new IndexOutOfBoundsException(String.format("Y (%s) is out of bounds (%s)", y, height));
        }

        // Calculate index
        return y * width + x;
    }

    //
    // Various calculations and utility methods
    //

    /**
     * Maintains {@link Size}, but will replace all cells with empty ones.
     */
    public void clear() {
        synchronized (this) {
            List<Cell> tmpCells = new ArrayList<>();
            for (int capacity = getCapacity(); capacity > 0; capacity--) {
                tmpCells.add(null);
            }
            this.cells = tmpCells;
        }
    }

    public void setRect(final @NotNull Rect rect) {
        synchronized (this) {
            if (rect.getWidth() == this.rect.getWidth() && rect.getHeight() == this.rect.getHeight()) {
                this.rect = rect;
            } else {
                Canvas tmpGrid = new Canvas(rect);
                Canvas.paintHard(tmpGrid, this);
                this.cells = tmpGrid.cells;
                this.rect = tmpGrid.rect;
            }
        }
    }

    /**
     * Takes the intersecting area between the target and source
     * (which is created based off of the start points),
     * and uses it to grab values from source and insert into target.
     * <p>
     * Be careful with bad point values, it might result in unexpected null values.
     *
     * @param targetGrid This is where values will be inserted to.
     * @param sourceGrid This is where values will be sourced from.
     * @see #paintTransparency(Canvas, Canvas)
     */
    public static void paintHard(@NotNull Canvas targetGrid, @NotNull Canvas sourceGrid) {
        // Get the start points for both grids
        Point targetStart = targetGrid.getRect().getStartPoint();
        Point sourceStart = sourceGrid.getRect().getStartPoint();

        // Create rectangles (starting at 0,0) for each MutableGrid
        Rect targetRect = new Rect(
                targetStart.getX(), targetStart.getY(),
                targetGrid.getRect().getWidth(), targetGrid.getRect().getHeight()
        );
        Rect sourceRect = new Rect(
                sourceStart.getX(), sourceStart.getY(),
                sourceGrid.getRect().getWidth(), sourceGrid.getRect().getHeight()
        );

        // Get the rect we're interested in.
        // This rect is from the perspective of an imaginary container that contains both of the grids.
        Rect intersection = Rect.intersection(targetRect, sourceRect);

        // Painting
        for (int y = intersection.getMinY(); y <= intersection.getMaxY(); y++) {
            for (int x = intersection.getMinX(); x <= intersection.getMaxX(); x++) {
                // Turn this into separate sets of relative points for target and source
                int targetRelX = x - targetStart.getX();
                int targetRelY = y - targetStart.getY();
                int sourceRelX = x - sourceStart.getX();
                int sourceRelY = y - sourceStart.getY();

                targetGrid.set(targetRelX, targetRelY, sourceGrid.get(sourceRelX, sourceRelY));
            }
        }
    }

    /**
     * Paints a {@link Canvas} based on values from another. Null or default color acts as transparency.
     * <br>
     * Takes the intersecting area between the target and source
     * (which is created based off of the start points),
     * and uses it to grab values from source and insert into target.
     * <br>
     * Be careful with bad point values, it might result in unexpected null values.
     *
     * @param targetGrid This is where values will be inserted to.
     * @param sourceGrid This is where values will be sourced from.
     * @see #paintHard(Canvas, Canvas)
     */
    public static void paintTransparency(@NotNull Canvas targetGrid, @NotNull Canvas sourceGrid) {
        // Get the start points for both grids
        Point targetStart = targetGrid.getRect().getStartPoint();
        Point sourceStart = sourceGrid.getRect().getStartPoint();

        // Create rectangles (starting at 0,0) for each MutableGrid
        Rect targetRect = new Rect(
                targetStart.getX(), targetStart.getY(),
                targetGrid.getRect().getWidth(), targetGrid.getRect().getHeight()
        );
        Rect sourceRect = new Rect(
                sourceStart.getX(), sourceStart.getY(),
                sourceGrid.getRect().getWidth(), sourceGrid.getRect().getHeight()
        );

        // Get the rect we're interested in.
        // This rect is from the perspective of an imaginary container that contains both of the grids.
        Rect intersection = Rect.intersection(targetRect, sourceRect);

        // Painting
        for (int y = intersection.getMinY(); y <= intersection.getMaxY(); y++) {
            for (int x = intersection.getMinX(); x <= intersection.getMaxX(); x++) {
                // Turn this into separate sets of relative points for target and source
                int targetRelX = x - targetStart.getX();
                int targetRelY = y - targetStart.getY();
                int sourceRelX = x - sourceStart.getX();
                int sourceRelY = y - sourceStart.getY();

                // Get a cell from our source grid
                Canvas.Cell sourceCell = sourceGrid.get(sourceRelX, sourceRelY);

                // If the cell is null, we don't have to do anything,
                // as we're instead using use the one from the target grid
                if (sourceCell != null) {
                    // Get a cell from our target grid
                    Canvas.Cell targetCell = targetGrid.get(targetRelX, targetRelY);

                    // If the source element is "transparent", we should use the target element
                    String codePoint = sourceCell.getCodepoint();
                    if (codePoint == null && targetCell != null) codePoint = targetCell.getCodepoint();

                    // If the source element is "transparent", we should use the target element
                    SGRSequence sequence = sourceCell.getSequence();
                    if (sequence == null || sequence.equals(SGRSequence.FG_DEFAULT) || sequence.equals(SGRSequence.BG_DEFAULT)) {
                        if (targetCell != null) sequence = targetCell.getSequence();
                    }

                    // Apply the resulting cell to the target grid
                    targetGrid.set(targetRelX, targetRelY, new Canvas.Cell(codePoint, sequence));
                }
            }
        }
    }

    //
    // Cell class
    //

    /**
     * A cell in the terminal.
     * <br><br>
     * This is designed to be immutable.
     * Whilst I get that's a pain in the ass and a half, it makes programming this shit a heck of a lot easier for me.
     * I might replace this with something more efficient later in a couple of months or so...
     * if I'm feeling like over-engineering something for no reason. We'll see. Well.. back to work.<br>
     * - me, to myself in a couple of months from the time of writing
     * <br>
     */
    public static class Cell {
        private final String codepoint;
        private final SGRSequence sequence;
        private final @Range(from = -1, to = 2) int characterWidth;

        //
        // Constructors
        //

        public Cell() {
            this.codepoint = null;
            this.sequence = null;
            this.characterWidth = 0;
        }

        public Cell(@Nullable String codepoint, @Nullable SGRSequence sequence) {
            // Checks
            if (codepoint != null && codepoint.codePointCount(0, codepoint.length()) != 1) {
                throw new IllegalArgumentException("The String must contain only a single CodePoint");
            }

            this.codepoint = codepoint;
            this.sequence = sequence;
            // todo test if this actually works
            this.characterWidth = codepoint == null ? 0 : WCWidth.wcwidth(codepoint.codePointAt(0));
        }

        //
        // Getters
        //

        public @Nullable String getCodepoint() {
            return codepoint;
        }

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
            if (!(obj instanceof Canvas.Cell)) {
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
}