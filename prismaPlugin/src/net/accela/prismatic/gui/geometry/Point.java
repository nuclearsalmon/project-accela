package net.accela.prismatic.gui.geometry;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a coordinate point. Supports negative values.
 */
public class Point {
    public static final Point ZERO = new Point(0, 0);
    public static final Point OFFSET_1x1 = new Point(1, 1);

    private final int x, y;

    //
    // Constructors
    //

    public Point() {
        this.x = 0;
        this.y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(@NotNull Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    //
    // Getters
    //

    /**
     * @return The X location
     */
    public final int getX() {
        return x;
    }

    /**
     * @return The Y location
     */
    public final int getY() {
        return y;
    }

    //
    // Calculations and utilities
    //

    /**
     * @param startPoint The {@link Point} at the start of the line.
     * @param endPoint   The {@link Point} at the end of the line.
     * @return The distance from startPoint to endPoint, squared.
     */
    public static double distanceSquared(@NotNull Point startPoint, @NotNull Point endPoint) {
        double px = endPoint.getX() - startPoint.getX();
        double py = endPoint.getY() - startPoint.getY();
        return px * px + py * py;
    }

    /**
     * @param startPoint The {@link Point} at the start of the line.
     * @param endPoint   The {@link Point} at the end of the line.
     * @return The distance from startPoint to endPoint.
     */
    public static double distance(@NotNull Point startPoint, @NotNull Point endPoint) {
        int px = endPoint.getX() - startPoint.getX();
        int py = endPoint.getY() - startPoint.getY();
        return Math.sqrt(px * px + py * py);
    }

    @Deprecated
    public @NotNull Point subtract(@NotNull Point subtraction) {
        return subtract(this, subtraction);
    }

    @Deprecated
    public static @NotNull Point subtract(@NotNull Point first, @NotNull Point subtraction) {
        return new Point(first.x - subtraction.x, first.y - subtraction.y);
    }

    @Deprecated
    public @NotNull Point add(@NotNull Point addition) {
        return add(this, addition);
    }

    @Deprecated
    public static @NotNull Point add(@NotNull Point first, @NotNull Point addition) {
        return new Point(first.x + addition.x, first.y + addition.y);
    }

    public @NotNull Point withRelative(@NotNull Point point) {
        return withRelative(point.getX(), point.getY());
    }

    public @NotNull Point withRelative(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public @NotNull Point withRelativeX(int x) {
        return new Point(this.x + x, this.y);
    }

    public @NotNull Point withRelativeY(int y) {
        return new Point(this.x, this.y + y);
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point point = (Point) obj;
            return point.x == this.x && point.y == this.y;
        }
        return super.equals(obj);
    }

    @NotNull
    @Override
    public String toString() {
        return this.getClass().getName() + "[x=" + this.x + ",y=" + this.y + "]";
    }

    //
    // Comparator
    //

    public static class Comparator implements java.util.Comparator<Point> {
        @Override
        public int compare(Point pointA, Point pointB) {
            if (pointA.getY() == pointB.getY()) {
                if (pointA.getX() == pointB.getX()) {
                    return 0;
                } else {
                    return Integer.compare(pointA.getX(), pointB.getX());
                }
            } else {
                return Integer.compare(pointA.getY(), pointB.getY());
            }
        }
    }
}
