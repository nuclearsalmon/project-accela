package net.accela.prisma.drawables;

import net.accela.ansi.Crayon;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.Drawable;
import net.accela.prisma.event.MouseInputEvent;
import net.accela.prisma.event.SpecialInputEvent;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.util.Canvas;
import net.accela.server.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class MouseCursor extends Drawable {
    final Canvas canvas;

    public MouseCursor() {
        super(new Rect(1, 1));
        canvas = new Canvas(getSize());
    }

    @Override
    protected @NotNull Canvas getCanvas() {
        int r = ThreadLocalRandom.current().nextInt(256);
        int g = ThreadLocalRandom.current().nextInt(256);
        int b = ThreadLocalRandom.current().nextInt(256);
        SGRSequence color = new Crayon().rgbBg(r, g, b).blackFg();

        canvas.set(0, 0, new Canvas.Cell(":", color));

        return canvas;
    }

    @Override
    public boolean wantsFocus() {
        return false;
    }

    @Override
    public boolean cursorEnabled() {
        return false;
    }

    @EventHandler
    void onMouseInput(MouseInputEvent event) throws NodeNotFoundException {
        setRect(new Rect(event.getPoint().getX(), event.getPoint().getY(), 1, 1));
    }

    @EventHandler
    void onSpecialInput(SpecialInputEvent event) throws NodeNotFoundException {
        // Shift must be held down
        if (!event.isShift()) return;

        // Direction
        SpecialInputEvent.SpecialKey key = event.getKey();
        if (key == SpecialInputEvent.SpecialKey.UP) {
            setRect(getRelativeRect().startPointSubtraction(new Point(0, 1)));
        } else if (key == SpecialInputEvent.SpecialKey.DOWN) {
            setRect(getRelativeRect().startPointAddition(new Point(0, 1)));
        } else if (key == SpecialInputEvent.SpecialKey.RIGHT) {
            setRect(getRelativeRect().startPointAddition(new Point(1, 0)));
        } else if (key == SpecialInputEvent.SpecialKey.LEFT) {
            setRect(getRelativeRect().startPointSubtraction(new Point(1, 0)));
        }
    }
}
