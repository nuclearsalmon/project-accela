package net.accela.prisma.gui.drawable;

import net.accela.prisma.annotation.PointMutable;
import net.accela.prisma.file.AnsiFile;
import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Point;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.text.TextGrid;
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
    public @NotNull TextGrid getTextGrid() throws NodeNotFoundException {
        return ansiFile.getTextGrid();
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
        return new Rect(point, getTextGrid().getSize());
    }
}
