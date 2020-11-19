package net.accela.prisma.event;

import net.accela.prisma.geometry.Point;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Called when receiving {@link Point} input
 */
public class PointInputEvent extends InputEvent {
    final Point point;

    public PointInputEvent(@NotNull Plugin caller, Point point) {
        super(caller);
        this.point = point;
    }

    @NotNull
    public Point getPoint() {
        return point;
    }
}
