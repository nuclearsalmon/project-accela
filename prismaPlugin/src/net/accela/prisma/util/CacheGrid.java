package net.accela.prisma.util;

import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Grid represents a 2 dimensional grid. Uses int width & height pairs internally.
 * Behaves the way you'd expect, with index 0 always being the beginning
 *
 * @param <E> the type of elements in this grid
 */
public final class CacheGrid<E> {
    List<E> elements;
    Size size;

    /**
     * @param sideSize The size of the sides, AKA both width and height
     */
    public CacheGrid(int sideSize) throws RectOutOfBoundsException {
        this(new Size(sideSize));
    }

    public CacheGrid(int width, int height) throws RectOutOfBoundsException {
        this(new Size(width, height));
    }

    /**
     * @param size The {@link Size} of the {@link CacheGrid}
     */
    public CacheGrid(@NotNull Size size) throws RectOutOfBoundsException {
        this.size = size;
        clear();
    }

    /**
     * @return all elements of the grid
     */
    public @NotNull List<E> getElements() {
        return elements;
    }

    public @NotNull List<E> getElements(@NotNull Point start, @NotNull Point end) {
        return elements.subList(getIndex(start.getX(), start.getY()), getIndex(end.getX(), end.getY()));
    }

    /**
     * @return iterator for a grid
     */
    public @NotNull Iterator<E> iterator() {
        return getElements().iterator();
    }

    /**
     * Returns the element at position (x,y).
     *
     * @return the element at position (x,y)
     */
    public @Nullable E get(Point coordinate) {
        return elements.get(getIndex(coordinate.getX(), coordinate.getY()));
    }

    /**
     * Returns the element at position (x,y).
     *
     * @return the element at position (x,y)
     */
    public @Nullable E get(int x, int y) {
        return elements.get(getIndex(x, y));
    }

    /**
     * Returns the element at index idx.
     *
     * @return the element at given index
     */
    public @Nullable E get(int idx) {
        return elements.get(idx);
    }

    /**
     * Puts an element to the position idx
     *
     * @param element    to be added
     * @param coordinate position to add element to
     */
    public void set(@NotNull Point coordinate, @Nullable E element) {
        set(getIndex(coordinate.getX(), coordinate.getY()), element);
    }

    /**
     * Puts an element to the position idx
     *
     * @param element to be added
     * @param x       position x to add element to
     * @param y       position y to add element to
     */
    public void set(int x, int y, @Nullable E element) {
        set(getIndex(x, y), element);
    }

    /**
     * Puts an element to the position idx
     *
     * @param element to be added
     * @param idx     to add element at
     */
    public synchronized void set(int idx, @Nullable E element) {
        if (idx > elements.size()) throw new IndexOutOfBoundsException();
        elements.set(idx, element);
    }

    /**
     * Takes the intersecting area between the target and source
     * (which is created based off of the start points),
     * and uses it to grab values from source and insert into target.
     * <p>
     * Be careful with bad point values, it might result in unexpected null values.
     *
     * @param targetStart A {@link Point} representing the start of the target
     * @param target      This is where values will be inserted
     * @param sourceStart A {@link Point} representing the start of the source
     * @param source      This is where values will be sourced
     */
    public static <T> void overlay(@NotNull Point targetStart, @NotNull CacheGrid<T> target,
                                   @NotNull Point sourceStart, @NotNull CacheGrid<T> source) {
        // Create areas (starting at 0,0) for each CacheGrid
        Rect targetRect = new Rect(targetStart.getX(), targetStart.getY(), target.getWidth(), target.getHeight());
        Rect sourceRect = new Rect(sourceStart.getX(), sourceStart.getY(), source.getWidth(), source.getHeight());

        // Get the area we're interested in
        Rect intersection = Rect.intersection(targetRect, sourceRect);
        for (int y = intersection.getMinY(); y <= intersection.getMaxY(); y++) {
            for (int x = intersection.getMinX(); x <= intersection.getMaxX(); x++) {
                // Turn this into separate sets of relative points for target and source
                int targetRelX = x - targetStart.getX();
                int targetRelY = y - targetStart.getY();
                int sourceRelX = x - sourceStart.getX();
                int sourceRelY = y - sourceStart.getY();

                //System.out.println("i(" + x + "," + y + ") -> t(" + targetRelX + "," + targetRelY + ") + s(" + sourceRelX + "," + sourceRelY + ")");
                target.set(targetRelX, targetRelY, source.get(sourceRelX, sourceRelY));
            }
        }

        /* IF THE ABOVE BREAKS, USE THIS - THIS IS WHAT WAS USED BEFORE THE EDIT
        Rect intersect = Rect.intersection(targetRect, sourceRect);
        for (int x = intersect.getMinX(); x <= intersect.getMaxX(); x++) {
            for (int y = intersect.getMinY(); y <= intersect.getMaxY(); y++) {
                target.set(x, y, source.get(x - sourceStart.getX(), y - sourceStart.getY()));
            }
        }
         */

        /* EVEN OLDER CODE, NOT RECOMMENDED!!!
        Rect intersect = Rect.intersection(targetRect, sourceRect);
        for (int x = intersect.getMinX(); x <= intersect.getMaxX(); x++) {
            for (int y = intersect.getMinY(); y <= intersect.getMaxY(); y++) {
                int relativeX = x - sourceRect.getMinX();
                int relativeY = y - sourceRect.getMinY();

                target.set(x, y, source.get(relativeX, relativeY));
            }
        }
        */
    }

    /**
     * Destructive resizing, can be used to downscale destructively
     * or to upscale with null elements.
     * <p>
     * Be careful with bad point values, as it may result in unexpected null values.
     *
     * @param rect The rect is used to get values for width, height,
     *             along with serving as a starting {@link Point}
     */
    public void resize(@NotNull Rect rect) {
        resize(rect.getSize(), rect.getStartPoint());
    }

    /**
     * Destructive resizing, can be used to downscale destructively
     * or to upscale with null elements.
     * <p>
     * Be careful with bad point values, as it may result in unexpected null values.
     */
    public void resize(@NotNull Size size, @NotNull Point point) {
        CacheGrid<E> tmpGrid = new CacheGrid<>(size);
        CacheGrid.overlay(point, tmpGrid, point, this);
        this.elements = tmpGrid.elements;
        this.size = size;
    }

    /**
     * Maintains {@link Size}, but will replace all elements with null.
     * Same as {@link CacheGrid#fill(Object element)} but with null as argument.
     */
    public void clear() {
        fill(null);
    }

    /**
     * Maintains {@link Size}, but will replace all elements with with an element of your choice.
     *
     * @param element The element to replace with
     */
    public void fill(@Nullable E element) {
        this.elements = new ArrayList<>(Collections.nCopies(getCapacity(), element));
    }

    /**
     * @return The capacity; width * height, or how many elements this {@link CacheGrid} can hold.
     */
    public int getCapacity() {
        return size.getCapacity();
    }

    /**
     * @return number of elements in grid, including null elements.
     */
    public int getSizeAsInt() {
        return elements.size();
    }

    /**
     * @return The {@link Size} of this CacheGrid.
     */
    public @NotNull Size getSize() {
        return size;
    }

    /**
     * @return The {@link Size#getWidth()} of this CacheGrid.
     */
    public int getWidth() {
        return size.getWidth();
    }

    /**
     * @return The {@link Size#getHeight()} of this CacheGrid.
     */
    public int getHeight() {
        return size.getHeight();
    }

    /**
     * Returns the x coordinate from the index.
     *
     * @return x coordinate of the index
     */
    public int getX(int index) {
        return index % getWidth();
    }

    /**
     * Returns the y coordinate from the index.
     *
     * @return y coordinate of the index
     */
    public int getY(int index) {
        return index / getWidth();
    }

    /**
     * Returns index of element at (x,y).
     *
     * @return index of the coordinates
     */
    public int getIndex(int x, int y) {
        return y * getWidth() + x;
    }
}
