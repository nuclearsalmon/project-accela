package net.accela.prismatic.ui.drawable;

import net.accela.prismatic.CursorMode;
import net.accela.prismatic.Drawable;
import net.accela.prismatic.annotation.PointMutable;
import net.accela.prismatic.file.ansi.AnsiFile;
import net.accela.prismatic.ui.geometry.Point;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.geometry.Size;
import net.accela.prismatic.ui.text.TextGrid;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class AnsiGraphic extends Drawable implements PointMutable {
    final AnsiFile ansiFile;
    final TextGrid textGrid;

    public AnsiGraphic(@NotNull AnsiFile ansiFile, @NotNull Plugin plugin) {
        super(new Rect(ansiFile.getTextGrid().getSize()), plugin);
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
    public @NotNull TextGrid getTextGrid() {
        return ansiFile.getTextGrid();
    }

    //
    // Positioning
    //

    /**
     * @param point The relative position of this.
     */
    @Override
    public void setRelativePoint(@NotNull Point point) {
        internalSetPoint(point);
    }

    @Override
    protected void onResizeBeforePainting(@NotNull Size oldSize, @NotNull Size newSize) {
        textGrid.resize(newSize);
    }
}
