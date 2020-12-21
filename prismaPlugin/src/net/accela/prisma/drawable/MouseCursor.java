package net.accela.prisma.drawable;

import net.accela.ansi.Crayon;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.Drawable;
import net.accela.prisma.event.MouseInputEvent;
import net.accela.prisma.event.SpecialInputEvent;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.property.PointMutable;
import net.accela.prisma.util.canvas.Canvas;
import net.accela.prisma.util.canvas.Cell;
import net.accela.server.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class MouseCursor extends Drawable implements PointMutable {
    final Canvas canvas;
    @NotNull Point point = new Point(0, 0);

    public MouseCursor() {
        canvas = new Canvas(getSize());
    }

    //
    // Properties
    //

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public @NotNull CursorMode getCursorMode() {
        return CursorMode.NONE;
    }

    @Override
    public boolean cursorEnabled() {
        return false;
    }

    //
    // Positioning
    //

    /**
     * @param point The relative position of this.
     */
    @Override
    public void setRelativePoint(@NotNull Point point) throws NodeNotFoundException {
        this.point = point;
    }

    /**
     * @return The size and relative position of this {@link Drawable}.
     */
    @Override
    public @NotNull Rect getRelativeRect() throws NodeNotFoundException {
        return new Rect(point);
    }

    /**
     * @return This {@link Drawable}'s relative position.
     */
    @Override
    public @NotNull Point getRelativePoint() {
        return point;
    }

    //
    // Painting
    //

    @Override
    @NotNull
    public Canvas getCanvas() {
        int r = ThreadLocalRandom.current().nextInt(256);
        int g = ThreadLocalRandom.current().nextInt(256);
        int b = ThreadLocalRandom.current().nextInt(256);
        SGRSequence color = new Crayon().rgbBg(r, g, b).blackFg();

        canvas.set(0, 0, new Cell(":", color));

        return canvas;
    }

    //
    // Event handling
    //

    @EventHandler
    void onMouseInput(MouseInputEvent event) throws NodeNotFoundException {
        setRelativePoint(event.getPoint());
    }

    @EventHandler
    void onSpecialInput(SpecialInputEvent event) throws NodeNotFoundException {
        // Shift must be held down
        if (!event.isShift()) return;

        // Direction
        SpecialInputEvent.SpecialKey key = event.getKey();
        if (key == SpecialInputEvent.SpecialKey.UP) {
            setRelativePoint(getRelativePoint().subtract(new Point(0, 1)));
        } else if (key == SpecialInputEvent.SpecialKey.DOWN) {
            setRelativePoint(getRelativePoint().add(new Point(0, 1)));
        } else if (key == SpecialInputEvent.SpecialKey.RIGHT) {
            setRelativePoint(getRelativePoint().add(new Point(1, 0)));
        } else if (key == SpecialInputEvent.SpecialKey.LEFT) {
            setRelativePoint(getRelativePoint().subtract(new Point(1, 0)));
        }
    }
}
