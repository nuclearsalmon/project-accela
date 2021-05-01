package net.accela.prisma.annotation;

import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Point;
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
