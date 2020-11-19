package net.accela.prisma.event;

import net.accela.prisma.geometry.Point;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Called when receiving mouse input
 */
public class MouseInputEvent extends InputEvent {
    @SuppressWarnings("unused")
    public enum MouseInputType {
        MOTION,
        LEFT,
        RIGHT,
        MIDDLE,
        RELEASE,
        SCROLL_UP,
        SCROLL_DOWN,
        SCROLL_LEFT,
        SCROLL_RIGHT,
        EXTRA_0,
        EXTRA_1,
        EXTRA_2,
        EXTRA_3
    }

    final Point point;
    final MouseInputType mouseInputType;
    final boolean shift;
    final boolean meta;
    final boolean control;

    public MouseInputEvent(@NotNull Plugin caller, int x, int y, @NotNull MouseInputType mouseInputType) {
        this(caller, x, y, mouseInputType, false, false, false);
    }

    public MouseInputEvent(@NotNull Plugin caller, int x, int y, @NotNull MouseInputType mouseInputType,
                           boolean shift, boolean meta, boolean control) {
        super(caller);
        this.point = new Point(x, y);
        this.mouseInputType = mouseInputType;
        this.shift = shift;
        this.meta = meta;
        this.control = control;
    }

    @NotNull
    public Point getPoint() {
        return point;
    }

    @NotNull
    public MouseInputType getMouseInputType() {
        return mouseInputType;
    }

    public boolean isShift() {
        return shift;
    }

    public boolean isMeta() {
        return meta;
    }

    public boolean isControl() {
        return control;
    }


    @Override
    public @NotNull String toString() {
        return this.getClass().getName() +
                "[x=" + point.getX() + ",y=" + point.getY() + ",mouseInputType=" + mouseInputType + ",shift=" + shift +
                ",meta=" + meta + ",control=" + control + "]";
    }
}
