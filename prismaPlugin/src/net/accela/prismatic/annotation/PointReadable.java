package net.accela.prismatic.annotation;

import net.accela.prismatic.Drawable;
import net.accela.prismatic.ui.geometry.Point;
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
    Point getAbsolutePoint();
}
