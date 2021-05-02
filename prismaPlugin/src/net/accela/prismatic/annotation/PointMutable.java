package net.accela.prismatic.annotation;

import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Point;
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
