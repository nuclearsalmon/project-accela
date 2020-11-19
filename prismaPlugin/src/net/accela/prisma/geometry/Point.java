package net.accela.prisma.geometry;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a coordinate point. Supports negative values.
 */
public class Point {
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
}
