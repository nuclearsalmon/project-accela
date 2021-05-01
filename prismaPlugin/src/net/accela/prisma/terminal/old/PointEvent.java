package net.accela.prisma.terminal.old;

import net.accela.prisma.gui.geometry.Point;
import net.accela.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called when receiving {@link Point} input
 */
@Deprecated
public class PointEvent extends Event {
    final Point point;

    public PointEvent(Point point) {
        this.point = point;
    }

    @NotNull
    public Point getPoint() {
        return point;
    }
}
