package net.accela.prismatic.gui.drawable.container;

import net.accela.prismatic.Main;
import net.accela.prismatic.annotation.RectMutable;
import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.DrawableContainer;
import net.accela.prismatic.gui.drawabletree.Branch;
import net.accela.prismatic.gui.drawabletree.DrawableTree;
import net.accela.prismatic.gui.drawabletree.Node;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Point;
import net.accela.prismatic.gui.geometry.Rect;
import net.accela.prismatic.gui.geometry.Size;
import net.accela.prismatic.gui.geometry.exception.RectOutOfBoundsException;
import net.accela.prismatic.gui.text.BasicTextGrid;
import net.accela.prismatic.gui.text.TextCharacter;
import net.accela.prismatic.gui.text.TextGrid;
import net.accela.server.AccelaAPI;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

public class BufferedContainer extends DrawableContainer implements RectMutable {
    final BasicTextGrid backBuffer;
    final BasicTextGrid frontBuffer;

    public BufferedContainer() {
        this(new Rect());
    }

    public BufferedContainer(@NotNull Rect rect) {
        super(rect);
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
    public boolean cursorEnabled() {
        return true;
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
    public void setRelativePoint(@NotNull Point point) throws NodeNotFoundException {
        internalSetPoint(point);
    }

    /**
     * @param rect The size and relative position of this.
     * @see RectMutable#setAbsoluteRect(Rect)
     */
    @Override
    public void setRelativeRect(@NotNull Rect rect) throws NodeNotFoundException {
        internalSetRect(rect);
    }

    /**
     * @param size The size of this.
     */
    @Override
    public void setSize(@NotNull Size size) throws NodeNotFoundException {
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
    // Container methods
    //

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO: 12/7/20 remove the plugin argument and instantiate drawable with plugins directly
    public synchronized void attach(@NotNull Drawable drawable, @NotNull Plugin plugin) throws RectOutOfBoundsException, NodeNotFoundException {
        if (Main.DBG_RESPECT_CONTAINER_BOUNDS
                && !Rect.contains(getZeroRect(), drawable.getRelativeRect())) {
            throw new RectOutOfBoundsException("Drawable does not fit within the container");
        }

        // Attach
        getBranch().newNode(drawable);

        // Register any events
        AccelaAPI.getPluginManager().registerEvents(drawable, drawable.findPlugin(), drawable.getChannel());

        // Focus
        if (Main.DBG_FOCUS_ON_DRAWABLE_CONTAINER_ATTACHMENT) {
            getBranch().setFocusedNode(drawable.findNode());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void detach(@NotNull Drawable drawable) throws NodeNotFoundException, IOException {
        // Get the rect before detaching, we're going to need it later
        Rect rect = drawable.getAbsoluteRect();

        // Kill node
        Node node = DrawableTree.getNode(drawable);
        if (node != null) node.kill();

        // Unregister events
        AccelaAPI.getPluginManager().unregisterEvents(drawable);

        // Redraw the now empty rect
        paint(rect);

        // Focusing
        List<Node> nodes = getBranch().getChildNodeList();
        Node focusNode = nodes.size() > 0 ? nodes.get(0) : null;
        getBranch().setFocusedNode(focusNode);
    }

    //
    // Painting
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
        final List<Node> nodes = getBranch().getIntersectingNodes(targetRect);

        // Paint the canvas
        // Iterate in reverse
        ListIterator<Node> nodeIterator = nodes.listIterator(nodes.size());
        while (nodeIterator.hasPrevious()) {
            // Get drawable
            final Drawable drawable = nodeIterator.previous().getDrawable();

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
    public synchronized void paint(@NotNull Rect rect) throws NodeNotFoundException, IOException {
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
        final List<Node> nodes = getBranch().getIntersectingNodes(targetRect);

        // Paint the canvas
        // Iterate in reverse
        ListIterator<Node> nodeIterator = nodes.listIterator(nodes.size());
        while (nodeIterator.hasPrevious()) {
            // Get drawable
            final Drawable drawable = nodeIterator.previous().getDrawable();

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

        Rect paintRect = rect.startPointAddition(getRelativeRect().getStartPoint());

        Node selfNode = findNode();
        Branch parentNode = selfNode.getParent();
        if (parentNode != null) parentNode.getDrawable().paint(paintRect);
        else selfNode.getWindowManager().paint(paintRect);
    }

    @Override
    @NotNull
    public TextGrid getTextGrid() throws NodeNotFoundException {
        return frontBuffer;
    }

    @Override
    @NotNull
    public TextGrid getTextGrid(@NotNull Rect rect) throws NodeNotFoundException {
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
