package net.accela.prismatic.gui.drawable;

import net.accela.prismatic.annotation.PointMutable;
import net.accela.prismatic.file.AnsiFile;
import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Point;
import net.accela.prismatic.gui.geometry.Rect;
import net.accela.prismatic.gui.geometry.Size;
import net.accela.prismatic.gui.text.TextGrid;
import org.jetbrains.annotations.NotNull;

public class AnsiGraphic extends Drawable implements PointMutable {
    final AnsiFile ansiFile;
    final TextGrid textGrid;

    public AnsiGraphic(@NotNull AnsiFile ansiFile) {
        super(new Rect(ansiFile.getTextGrid().getSize()));
        this.ansiFile = ansiFile;
        this.textGrid = ansiFile.getTextGrid();
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
        internalSetPoint(point);
    }

    @Override
    protected void onResizeBeforePainting(@NotNull Size oldSize, @NotNull Size newSize) {
        textGrid.resize(newSize);
    }
}
