package net.accela.prisma.property;

import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import org.jetbrains.annotations.NotNull;

public interface PointMutable extends PointReadable {
    /**
     * @param point The relative position of this.
     */
    void setRelativePoint(@NotNull Point point) throws NodeNotFoundException;

    /**
     * @param point The absolute position of this.
     */
    default void setAbsolutePoint(@NotNull Point point) throws NodeNotFoundException {
        Point absoluteCurrentPoint = getAbsolutePoint();
        Point relativePoint = new Point(
                point.getX() - absoluteCurrentPoint.getX(),
                point.getY() - absoluteCurrentPoint.getY()
        );
        setRelativePoint(relativePoint);
    }
}
