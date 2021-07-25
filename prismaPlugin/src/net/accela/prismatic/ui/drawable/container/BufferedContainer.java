package net.accela.prismatic.ui.drawable.container;

import net.accela.prismatic.CursorMode;
import net.accela.prismatic.Drawable;
import net.accela.prismatic.DrawableContainer;
import net.accela.prismatic.Main;
import net.accela.prismatic.annotation.ItemPainter;
import net.accela.prismatic.annotation.RectMutable;
import net.accela.prismatic.ui.geometry.Point;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.geometry.Size;
import net.accela.prismatic.ui.text.BasicTextGrid;
import net.accela.prismatic.ui.text.TextCharacter;
import net.accela.prismatic.ui.text.TextGrid;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

public class BufferedContainer extends DrawableContainer implements RectMutable {
    final BasicTextGrid backBuffer;
    final BasicTextGrid frontBuffer;

    public BufferedContainer(@NotNull Plugin plugin) {
        this(new Rect(), plugin);
    }

    public BufferedContainer(@NotNull Rect rect, @NotNull Plugin plugin) {
        super(rect, plugin);
        backBuffer = new BasicTextGrid(rect.getSize());
        frontBuffer = new BasicTextGrid(rect.getSize());
    }

    //
    // General Properties
    //

    /**
     * @return whether or not this {@link Drawable} is eligible for being focused.
     */
    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public @NotNull CursorMode getCursorMode() {
        return CursorMode.RENDERED;
    }

    @Override
    public boolean transparent() {
        return false;
    }

    //
    // Size and position
    //

    /**
     * @param point The relative position of this.
     */
    @Override
    public void setRelativePoint(@NotNull Point point) {
        internalSetPoint(point);
    }

    /**
     * @param rect The size and relative position of this.
     * @see RectMutable#setAbsoluteRect(Rect)
     */
    @Override
    public void setRelativeRect(@NotNull Rect rect) {
        internalSetRect(rect);
    }

    /**
     * @param size The size of this.
     */
    @Override
    public void setSize(@NotNull Size size) {
        internalSetSize(size);
    }

    /**
     * @param width The width of this.
     */
    @Override
    public void setWidth(@Range(from = 1, to = Integer.MAX_VALUE) int width) {
        internalSetSize(new Size(width, getHeight()));
    }

    /**
     * @param height The height of this.
     */
    @Override
    public void setHeight(@Range(from = 1, to = Integer.MAX_VALUE) int height) {
        internalSetSize(new Size(getWidth(), height));
    }

    //
    // Rendering
    //

    /**
     * Render the container contents in the back buffer
     */
    public void renderBuffer() {
        renderBuffer(getZeroRect());
    }

    /**
     * Render the container contents in the back buffer
     */
    public void renderBuffer(@NotNull Rect rect) {
        // Establish current boundaries
        final Rect containerBounds = getZeroRect();

        // Get the intersection of the rect of this container vs the requested rect
        final Rect targetRect = Rect.intersection(containerBounds, rect);

        // Check out of bounds behaviour
        if (targetRect == null) {
            if (Main.DBG_RESPECT_TERMINAL_BOUNDS) {
                throw new IllegalStateException(
                        "\n" + rect + "\n is completely outside the terminal boundaries \n" + containerBounds);
            } else {
                return;
            }
        }

        // Clear the target area in the buffer to prevent flickering
        for (int y = targetRect.getMinY(); y <= targetRect.getMaxY(); y++) {
            for (int x = targetRect.getMinX(); x <= targetRect.getMaxX(); x++) {
                backBuffer.setCharacterAt(x, y, TextCharacter.DEFAULT);
            }
        }

        // Get drawable that intersect with the rectangle
        final List<Drawable> intersectingDrawables = getIntersectingDrawables(targetRect);

        // Paint the canvas
        // Iterate in reverse
        ListIterator<Drawable> nodeIterator = intersectingDrawables.listIterator(intersectingDrawables.size());
        while (nodeIterator.hasPrevious()) {
            // Get drawable
            final Drawable drawable = nodeIterator.previous();

            // Skip hidden
            if (!drawable.show) continue;

            // Get rectangles and intersect them
            final Rect drawableRect = drawable.getRelativeRect();
            final Rect targetIntersection = Rect.intersection(targetRect, drawableRect);
            if (targetIntersection == null) throw new IllegalStateException("THIS SHOULD NOT BE NULL");

            // Get canvas
            final TextGrid drawableTextGrid = drawable.getTextGrid();

            // Only paint to main canvas after validation
            if (drawableTextGrid.getSize().equals(drawableRect.getSize())) {
                // Insert into backBuffer
                drawableTextGrid.copyTo(
                        backBuffer,
                        targetIntersection.getMinY() - drawableRect.getMinY(),
                        targetIntersection.getHeight(),

                        targetIntersection.getMinX() - drawableRect.getMinX(),
                        targetIntersection.getWidth(),

                        targetIntersection.getMinY(), targetIntersection.getMinX()
                );
            } else {
                // Warn and add Drawable to list of bad Drawables
                String warnMsg = drawable.toString() + "\n"
                        + drawableTextGrid + "\n"
                        + " tried to pass a BasicTextGrid with non-matching Size dimensions. The Drawable will be detached.";
                throw new IllegalStateException(warnMsg);
            }
        }
    }

    /**
     * Pushes backBuffer into frontBuffer and refreshes recursively to apply
     * any changes, thus displaying the drawable.
     */
    public void refreshBuffer() {
        backBuffer.copyTo(frontBuffer);
    }

    @Override
    public synchronized void render(@NotNull Rect rect) throws IOException {
        // Establish current boundaries
        final Rect containerBounds = getZeroRect();

        // Get the intersection of the rect of this container vs the requested rect
        final Rect targetRect = Rect.intersection(containerBounds, rect);

        // Check out of bounds behaviour
        if (targetRect == null) {
            if (Main.DBG_RESPECT_TERMINAL_BOUNDS) {
                throw new IllegalStateException(
                        "\n" + rect + "\n is completely outside the terminal boundaries \n" + containerBounds);
            } else {
                return;
            }
        }

        // Clear the target area in the buffer to prevent flickering
        for (int y = targetRect.getMinY(); y <= targetRect.getMaxY(); y++) {
            for (int x = targetRect.getMinX(); x <= targetRect.getMaxX(); x++) {
                backBuffer.setCharacterAt(x, y, TextCharacter.DEFAULT);
            }
        }

        // Get drawable that intersect with the rectangle
        final List<Drawable> intersectingDrawables = getIntersectingDrawables(targetRect);

        // Paint the canvas
        // Iterate in reverse
        ListIterator<Drawable> nodeIterator = intersectingDrawables.listIterator(intersectingDrawables.size());
        while (nodeIterator.hasPrevious()) {
            // Get drawable
            final Drawable drawable = nodeIterator.previous();

            // Get rectangles and intersect them
            final Rect drawableRect = drawable.getRelativeRect();
            final Rect targetIntersection = Rect.intersection(targetRect, drawableRect);

            if (targetIntersection == null) throw new IllegalStateException("THIS SHOULD NOT BE NULL");

            // Get canvas
            final TextGrid drawableTextGrid = drawable.getTextGrid();

            // Only paint to main canvas after validation
            if (drawableTextGrid.getSize().equals(drawableRect.getSize())) {
                // Insert into backBuffer
                drawableTextGrid.copyTo(
                        backBuffer,
                        targetIntersection.getMinY() - drawableRect.getMinY(),
                        targetIntersection.getHeight(),

                        targetIntersection.getMinX() - drawableRect.getMinX(),
                        targetIntersection.getWidth(),

                        targetIntersection.getMinY(), targetIntersection.getMinX()
                );
            } else {
                // Warn and add Drawable to list of bad Drawables
                String warnMsg = drawable.toString() + "\n"
                        + drawableTextGrid + "\n"
                        + " tried to pass a BasicTextGrid with non-matching Size dimensions. The Drawable will be detached.";
                throw new IllegalStateException(warnMsg);
            }
        }

        // Update frontBuffer
        backBuffer.copyTo(frontBuffer);

        // Paint
        Rect paintRect = rect.startPointAddition(getRelativeRect().getStartPoint());
        ItemPainter painter = getParentContainer();
        if (painter != null) painter.render(paintRect);
    }

    @Override
    @NotNull
    public TextGrid getTextGrid() {
        return frontBuffer;
    }

    @Override
    @NotNull
    public TextGrid getTextGrid(@NotNull Rect rect) {
        return frontBuffer.getCrop(rect);
    }

    //
    // Internal
    //

    @Override
    protected void onResizeBeforePainting(@NotNull Size oldSize, @NotNull Size newSize) {
        synchronized (this) {
            backBuffer.resize(newSize, TextCharacter.DEFAULT);
            frontBuffer.resize(newSize, TextCharacter.DEFAULT);
        }
    }
}
