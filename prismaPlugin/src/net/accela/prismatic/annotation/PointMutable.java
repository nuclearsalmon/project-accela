package net.accela.prismatic.annotation;

import net.accela.prismatic.ui.geometry.Point;
import org.jetbrains.annotations.NotNull;

public interface PointMutable extends PointReadable {
    /**
     * @param point The relative position of this.
     */
    void setRelativePoint(@NotNull Point point);

    /**
     * @param point The absolute position of this.
     */
    default void setAbsolutePoint(@NotNull Point point) {
        Point absoluteCurrentPoint = getAbsolutePoint();
        Point relativePoint = new Point(
                point.getX() - absoluteCurrentPoint.getX(),
                point.getY() - absoluteCurrentPoint.getY()
        );
        setRelativePoint(relativePoint);
    }
}
