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
import net.accela.prismatic.gui.text.TextGrid;
import net.accela.server.AccelaAPI;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class SimpleContainer extends DrawableContainer implements RectMutable {
    protected Rect rect;

    public SimpleContainer() {
        this.rect = new Rect();
    }

    public SimpleContainer(@NotNull Rect rect) {
        this.rect = rect;
    }

    //
    // Properties
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
    // Position and size
    //

    /**
     * @return The size and relative position of this {@link Drawable}.
     */
    @Override
    public @NotNull Rect getRelativeRect() throws NodeNotFoundException {
        return rect;
    }

    /**
     * @return This {@link Drawable}'s relative position.
     */
    @Override
    public @NotNull Point getRelativePoint() {
        return rect.getStartPoint();
    }

    /**
     * @param point The relative position of this.
     */
    @Override
    public void setRelativePoint(@NotNull Point point) throws NodeNotFoundException {
        setRelativeRect(new Rect(point, rect.getSize()));
    }

    /**
     * @param rect The size and relative position of this.
     * @see RectMutable#setAbsoluteRect(Rect)
     */
    @Override
    public void setRelativeRect(@NotNull Rect rect) throws NodeNotFoundException {
        Rect oldRect = this.rect;
        this.rect = rect;
        Rect newRect = this.rect;
        paintAfterGeometryChange(oldRect, newRect);
    }

    /**
     * @param size The size of this.
     */
    @Override
    public void setSize(@NotNull Size size) throws NodeNotFoundException {
        setRelativeRect(new Rect(rect.getStartPoint(), size));
    }

    /**
     * @param width The width of this.
     */
    @Override
    public void setWidth(@Range(from = 1, to = Integer.MAX_VALUE) int width) {
        setRelativeRect(new Rect(rect.getStartPoint(), new Size(width, rect.getHeight())));
    }

    /**
     * @param height The height of this.
     */
    @Override
    public void setHeight(@Range(from = 1, to = Integer.MAX_VALUE) int height) {
        setRelativeRect(new Rect(rect.getStartPoint(), new Size(rect.getWidth(), height)));
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

    @Override
    public void paint(@NotNull Rect rect) throws NodeNotFoundException, IOException {
        // Attempt to calculate an intersecting Rect, and ensure it's within the boundaries of this container
        final Rect containerBounds = getZeroRect();
        final Rect targetRect = Rect.intersection(containerBounds, rect);
        if (Main.DBG_RESPECT_CONTAINER_BOUNDS && targetRect == null) throw new IllegalStateException(
                "\n" + rect + "\n is outside the container boundaries \n" + containerBounds);

        Rect paintRect = rect.startPointAddition(getRelativeRect().getStartPoint());

        Node selfNode = findNode();
        Branch parentNode = selfNode.getParent();
        if (parentNode != null) parentNode.getDrawable().paint(paintRect);
        else selfNode.getWindowManager().paint(paintRect);
    }

    @Override
    @NotNull
    public TextGrid getTextGrid() throws NodeNotFoundException {
        return getTextGrid(getZeroRect());
    }

    @Override
    @NotNull
    public TextGrid getTextGrid(@NotNull Rect rect) throws NodeNotFoundException {
        // Attempt to calculate an intersecting Rect, and ensure it's within the boundaries of this container
        final Rect containerBounds = getZeroRect();
        final Rect targetRect = Rect.intersection(containerBounds, rect);
        if (targetRect == null) throw new IllegalStateException(
                rect + " is outside the container boundaries " + containerBounds);

        // Create a new canvas
        TextGrid canvas = new BasicTextGrid(targetRect.getSize());

        // Get all drawable within the rectangle
        final List<Drawable> drawables = getIntersectingImmediateDrawables(targetRect);

        // Iterate in reverse
        ListIterator<Drawable> drawableIterator = drawables.listIterator(drawables.size());
        while (drawableIterator.hasPrevious()) {
            // Get drawable
            final Drawable drawable = drawableIterator.previous();

            // Get rectangles and intersect them
            final Rect drawableRect = drawable.getRelativeRect();
            final Rect targetIntersection = Rect.intersection(targetRect, drawableRect);

            if (targetIntersection == null) throw new IllegalStateException("rect is outside bounds");

            // Paint the BasicTextGrid
            drawable.getTextGrid().copyTo(canvas, drawableRect);
        }
        return canvas;
    }

    //
    // Internal
    //

    private List<@NotNull Drawable> getIntersectingImmediateDrawables(@NotNull Rect rect) throws NodeNotFoundException {
        final List<Drawable> drawables = new ArrayList<>();
        for (Node node : getBranch().getChildNodeList()) {
            Drawable drawable = node.getDrawable();
            if (rect.intersects(drawable.getRelativeRect())) {
                drawables.add(drawable);
            }
        }
        return drawables;
    }
}
