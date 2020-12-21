package net.accela.prisma.drawable;

import net.accela.prisma.Drawable;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.property.RectMutable;
import net.accela.prisma.util.ansi.AnsiFile;
import net.accela.prisma.util.canvas.Canvas;
import org.jetbrains.annotations.NotNull;

public class AnsiGraphic extends Drawable implements RectMutable {
    public AnsiGraphic(@NotNull AnsiFile ansiFile) {

    }

    /**
     * @return A {@link Rect} representing the relative size and position of this {@link Drawable}.
     * Relative, in this case, means from the perspective of the {@link Container} of this {@link Drawable}.
     */
    @Override
    public @NotNull Rect getRelativeRect() {
        return null;
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    @NotNull
    public Canvas getCanvas() throws NodeNotFoundException {
        return null;
    }
}
