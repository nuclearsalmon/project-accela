package net.accela.prisma.gui.drawable;

import net.accela.prisma.annotation.PointMutable;
import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Point;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.text.BasicTextGrid;
import net.accela.prisma.gui.text.TextCharacter;
import net.accela.prisma.gui.text.TextGrid;
import net.accela.prisma.gui.text.color.TextColor;
import net.accela.prisma.input.lanterna.actions.KeyStroke;
import net.accela.prisma.input.lanterna.actions.MouseAction;
import net.accela.server.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class MouseCursor extends Drawable implements PointMutable {
    final TextGrid canvas;
    @NotNull Point point = new Point(0, 0);

    public MouseCursor() {
        canvas = new BasicTextGrid(getSize());
    }

    //
    // Properties and flags
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

    @Override
    public boolean transparent() {
        return true;
    }

    //
    // Positioning
    //

    /**
     * @param point The relative position of this.
     */
    @Override
    public void setRelativePoint(@NotNull Point point) throws NodeNotFoundException {
        Rect oldRect = getRelativeRect();
        this.point = point;
        Rect newRect = getRelativeRect();
        paintAfterGeometryChange(oldRect, newRect);
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
    public TextGrid getTextGrid() {
        int r = ThreadLocalRandom.current().nextInt(256);
        int g = ThreadLocalRandom.current().nextInt(256);
        int b = ThreadLocalRandom.current().nextInt(256);

        canvas.setCharacterAt(0, 0, TextCharacter.fromCharacter(':', TextColor.ANSI.DEFAULT, new TextColor.RGB(r, g, b)));

        return canvas;
    }

    //
    // Event handling
    //

    @EventHandler
    void onKeyStroke(@NotNull KeyStroke keyStroke) {
        // Shift must be held down
        if (!keyStroke.isShiftDown()) return;

        switch (keyStroke.getKeyType()) {
            case MouseEvent:
                setRelativePoint(((MouseAction) keyStroke).getPosition());
                break;
            case ArrowUp:
                setRelativePoint(getRelativePoint().withRelative(0, -1));
                break;
            case ArrowDown:
                setRelativePoint(getRelativePoint().withRelative(0, 1));
                break;
            case ArrowLeft:
                setRelativePoint(getRelativePoint().withRelative(-1, 0));
                break;
            case ArrowRight:
                setRelativePoint(getRelativePoint().withRelative(1, 0));
                break;
        }
    }
}
