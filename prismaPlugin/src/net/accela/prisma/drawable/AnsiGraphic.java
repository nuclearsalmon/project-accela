package net.accela.prisma.drawable;

import net.accela.prisma.Drawable;
import net.accela.prisma.ansi.file.AnsiFile;
import net.accela.prisma.ansi.util.canvas.Canvas;
import net.accela.prisma.drawable.property.PointMutable;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import org.jetbrains.annotations.NotNull;

public class AnsiGraphic extends Drawable implements PointMutable {
    final AnsiFile ansiFile;

    Point point = new Point();

    public AnsiGraphic(@NotNull AnsiFile ansiFile) {
        this.ansiFile = ansiFile;
    }

    //
    // Properties and flags
    //

    /**
     * @return whether or not this {@link Drawable} is eligible for being focused.
     */
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
        return false;
    }

    //
    // File
    //

    public @NotNull AnsiFile getFile() {
        return ansiFile;
    }

    //
    // Painting
    //

    @Override
    public @NotNull Canvas getCanvas() throws NodeNotFoundException {
        return ansiFile.getCanvas();
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
     * @return This {@link Drawable}'s relative position.
     */
    @Override
    public @NotNull Point getRelativePoint() {
        return point;
    }

    /**
     * @return The size and relative position of this {@link Drawable}.
     */
    @Override
    public @NotNull Rect getRelativeRect() throws NodeNotFoundException {
        return new Rect(point, getCanvas().getSize());
    }
}
