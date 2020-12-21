package net.accela.prisma.util.canvas;

import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.property.SizeMutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a 2 dimensional canvas.
 * Behaves as expected, with index 0 always being the beginning (top left corner).
 */
public class Canvas implements SizeMutable {
    /**
     * {@link Cell}s can either be empty or null if its position on the {@link Canvas} isn't occupied.
     */
    List<@Nullable Cell> cells;
    /**
     * The {@link Size} of this {@link Canvas}.
     */
    Size size;

    public Canvas(@NotNull Size size) {
        this.size = size;
        clear();
    }

    /**
     * Clones a {@link Canvas}.
     *
     * @param canvas The {@link Canvas} to clone.
     */
    public Canvas(@NotNull Canvas canvas) {
        this.cells = canvas.cells;
        this.size = canvas.size;
    }

    //
    // Setters
    //

    /**
     * Puts a {@link Cell} to the position idx.
     *
     * @param cell       to be added.
     * @param coordinate position to add cell to.
     */
    public void set(@NotNull Point coordinate, @Nullable Cell cell) {
        set(getIndex(coordinate.getX(), coordinate.getY()), cell);
    }

    /**
     * Puts a {@link Cell} to the position idx.
     *
     * @param cell to be added.
     * @param x    position x to add cell to.
     * @param y    position y to add cell to.
     */
    public void set(int x, int y, @Nullable Cell cell) {
        set(getIndex(x, y), cell);
    }

    /**
     * Puts a {@link Cell} to the position idx
     *
     * @param cell to be added
     * @param idx  to add cell at
     */
    public synchronized void set(int idx, @Nullable Cell cell) {
        if (idx >= cells.size()) throw new IndexOutOfBoundsException();
        cells.set(idx, cell);
    }

    public void setSize(@NotNull Size size) {
        Canvas tmpCanvas = new Canvas(size);
        Canvas.paintHard(tmpCanvas, Point.ZERO, this, Point.ZERO);
        synchronized (this) {
            this.cells = tmpCanvas.cells;
            this.size = size;
        }
    }

    //
    // Getters
    //

    /**
     * @return The {@link Size} of this {@link Canvas}.
     */
    public @NotNull Size getSize() {
        return size;
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
     * @return iterator for a {@link Canvas}
     */
    public @NotNull Iterator<@Nullable Cell> iterator() {
        return cells.iterator();
    }

    /**
     * Returns the {@link Cell} at position (x,y).
     *
     * @return the {@link Cell} at position (x,y)
     */
    public @Nullable Cell get(@NotNull Point coordinate) {
        return cells.get(getIndex(coordinate.getX(), coordinate.getY()));
    }

    /**
     * Returns the {@link Cell} at position (x,y).
     *
     * @return the {@link Cell} at position (x,y)
     */
    public @Nullable Cell get(int x, int y) {
        return get(getIndex(x, y));
    }

    /**
     * Returns the {@link Cell} at index idx.
     *
     * @return the {@link Cell} at given index
     */
    public @Nullable Cell get(int idx) {
        return cells.get(idx);
    }

    /**
     * @return The capacity; width * height, or how many {@link Cell}s this {@link Canvas} can hold.
     */
    public int getCapacity() {
        return size.getCapacity();
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
        return index % size.getWidth();
    }

    /**
     * Returns the y coordinate from the index.
     *
     * @return y coordinate of the index
     */
    public int getY(int index) {
        return index / size.getWidth();
    }

    /**
     * Returns index of {@link Cell} at {@link Point} (x,y).
     *
     * @return index of the coordinates
     */
    public int getIndex(@NotNull Point point) {
        return getIndex(point.getX(), point.getY());
    }

    /**
     * Returns index of {@link Cell} at (x,y).
     *
     * @return index of the coordinate point
     */
    public int getIndex(int x, int y) {
        // Get dimensions
        int width = size.getWidth(), height = size.getHeight();

        // Ensure the coordinate point is within bounds
        if (x > width) {
            throw new IndexOutOfBoundsException(String.format("X (%s) is out of bounds (%s)", x, width));
        }
        if (y > height) {
            throw new IndexOutOfBoundsException(String.format("Y (%s) is out of bounds (%s)", y, height));
        }

        // Calculate index
        return y * width + x;
    }

    //
    // Painting
    //

    /**
     * Maintains {@link Size}, but will replace all {@link Cell}s with empty ones.
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

    public void fill(@NotNull Rect rect, @Nullable String codePoint, @Nullable SGRSequence sequence) {
        for (int y = rect.getMinY(); y < rect.getMaxY() + 1; y++) {
            for (int x = rect.getMinX(); x < rect.getMaxX() + 1; x++) {
                set(x, y, new Cell(codePoint, sequence));
            }
        }
    }

    /**
     * Paints a target {@link Canvas} by inserting values from an intersecting source {@link Canvas}.
     *
     * @param target      This is where values will be inserted to.
     * @param targetStart The start {@link Point} for the target {@link Canvas}
     * @param source      This is where values will be sourced from.
     * @param sourceStart The start {@link Point} for the source {@link Canvas}
     * @see #paintTransparency(Canvas, Point, Canvas, Point)
     */
    public static void paintHard(@NotNull Canvas target, @NotNull Point targetStart,
                                 @NotNull Canvas source, @NotNull Point sourceStart) {

        // Create rectangles (starting at 0,0) for each Mutable
        Rect targetRect = new Rect(
                targetStart.getX(), targetStart.getY(),
                target.getSize().getWidth(), target.getSize().getHeight()
        );
        Rect sourceRect = new Rect(
                sourceStart.getX(), sourceStart.getY(),
                source.getSize().getWidth(), source.getSize().getHeight()
        );

        // Get the rect we're interested in.
        // This rect is from the perspective of an imaginary container that contains both of the canvass.
        Rect intersection = Rect.intersection(targetRect, sourceRect);
        if (intersection == null) throw new IllegalStateException("Rect out of bounds");

        // Painting
        for (int y = intersection.getMinY(); y <= intersection.getMaxY(); y++) {
            for (int x = intersection.getMinX(); x <= intersection.getMaxX(); x++) {
                // Turn this into separate sets of relative points for target and source
                int targetRelX = x - targetStart.getX();
                int targetRelY = y - targetStart.getY();
                int sourceRelX = x - sourceStart.getX();
                int sourceRelY = y - sourceStart.getY();

                // todo remember to NOT include reset, it's not needed in this cell based design

                target.set(targetRelX, targetRelY, source.get(sourceRelX, sourceRelY));
            }
        }
    }

    /**
     * Paints a target {@link Canvas} by inserting values from an intersecting source {@link Canvas}.
     * Null or default color acts as transparency.
     *
     * @param target      This is where values will be inserted to.
     * @param targetStart The start {@link Point} for the target {@link Canvas}
     * @param source      This is where values will be sourced from.
     * @param sourceStart The start {@link Point} for the source {@link Canvas}
     * @see #paintHard(Canvas, Point, Canvas, Point)
     */
    public static void paintTransparency(@NotNull Canvas target, @NotNull Point targetStart,
                                         @NotNull Canvas source, @NotNull Point sourceStart) {

        // Create rectangles (starting at 0,0) for each Mutable
        Rect targetRect = new Rect(
                targetStart.getX(), targetStart.getY(),
                target.getSize().getWidth(), target.getSize().getHeight()
        );
        Rect sourceRect = new Rect(
                sourceStart.getX(), sourceStart.getY(),
                source.getSize().getWidth(), source.getSize().getHeight()
        );

        // Get the rect we're interested in.
        // This rect is from the perspective of an imaginary container that contains both of the canvass.
        Rect intersection = Rect.intersection(targetRect, sourceRect);
        if (intersection == null) return;

        // Painting
        for (int y = intersection.getMinY(); y <= intersection.getMaxY(); y++) {
            for (int x = intersection.getMinX(); x <= intersection.getMaxX(); x++) {
                // Turn this into separate sets of relative points for target and source
                int targetRelX = x - targetStart.getX();
                int targetRelY = y - targetStart.getY();
                int sourceRelX = x - sourceStart.getX();
                int sourceRelY = y - sourceStart.getY();

                // Get a cell from our source canvas
                Cell sourceCell = source.get(sourceRelX, sourceRelY);

                // If the cell is null, we don't have to do anything,
                // as we're instead using use the one from the target canvas
                if (sourceCell != null) {
                    // Get a cell from our target canvas
                    Cell targetCell = target.get(targetRelX, targetRelY);

                    // If the source element is "transparent", we should use the target element
                    String codePoint = sourceCell.getCodepoint();
                    if (codePoint == null && targetCell != null) codePoint = targetCell.getCodepoint();

                    // If the source element is "transparent", we should use the target element
                    SGRSequence sequence = sourceCell.getSequence();
                    if (sequence == null && targetCell != null) sequence = targetCell.getSequence();

                    // Apply the resulting cell to the target canvas
                    target.set(targetRelX, targetRelY, new Cell(codePoint, sequence));
                }
            }
        }
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return super.toString() + " : \nsize=" + size;
    }
}