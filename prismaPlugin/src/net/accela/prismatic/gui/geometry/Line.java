package net.accela.prismatic.gui.geometry;

import org.jetbrains.annotations.NotNull;

/**
 * A straight {@link Line} between two {@link Point}s.
 *
 * @see Point
 */
public class Line implements Shape {
    private final Point startPoint;
    private final Point endPoint;

    public Line(int startX, int startY, int endX, int endY) {
        this.startPoint = new Point(startX, startY);
        this.endPoint = new Point(endX, endY);
    }

    public Line(@NotNull Point startPoint, @NotNull Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    /**
     * @return The start {@link Point} of the {@link Line}.
     */
    public final Point getStartPoint() {
        return startPoint;
    }

    /**
     * @return The end {@link Point} of the {@link Line}.
     */
    public final Point getEndPoint() {
        return endPoint;
    }

    /**
     * @return The length of the line.
     */
    public final double length() {
        return Point.distance(startPoint, endPoint);
    }

    /**
     * @return The length of the line, squared.
     */
    public final double lengthSquared() {
        return Point.distanceSquared(startPoint, endPoint);
    }

    //
    // Shape overrides
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Rect getBounds() {
        return new Rect(startPoint, endPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@NotNull Point point) {
        return getBounds().contains(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@NotNull Rect rect) {
        return getBounds().contains(rect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersects(@NotNull Rect rect) {
        return getBounds().intersects(rect);
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            Line line = (Line) obj;
            return this.startPoint.equals(line.startPoint) && this.endPoint.equals(line.endPoint);
        }
        return super.equals(obj);
    }

    @NotNull
    @Override
    public String toString() {
        return this.getClass().getName() + "[start=" + this.startPoint + ",end=" + this.endPoint + "]";
    }
}
