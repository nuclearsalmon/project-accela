package net.accela.prisma.drawable.property;

import net.accela.prisma.Drawable;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
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
