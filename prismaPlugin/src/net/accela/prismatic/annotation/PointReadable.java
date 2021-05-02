package net.accela.prismatic.annotation;

import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Point;
import org.jetbrains.annotations.NotNull;

public interface PointReadable {
    /**
     * @return This {@link Drawable}'s relative position.
     */
    @NotNull
    Point getRelativePoint();

    /**
     * @return This {@link Drawable}'s absolute position.
     */
    @NotNull
    Point getAbsolutePoint() throws NodeNotFoundException;
}
