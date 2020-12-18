package net.accela.prisma;

import net.accela.prisma.event.ActivationEvent;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import net.accela.prisma.util.Canvas;
import net.accela.prisma.util.drawabletree.Branch;
import net.accela.prisma.util.drawabletree.DrawableTree;
import net.accela.prisma.util.drawabletree.Node;
import net.accela.server.AccelaAPI;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A borderless container for drawables
 */
public class DrawableContainer extends Drawable implements Container {
    public DrawableContainer(@NotNull Rect rect) {
        super(rect);
    }

    @Override
    public boolean wantsFocus() {
        return true;
    }

    public @NotNull Branch getBranch() throws NodeNotFoundException {
        return (Branch) super.getNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO: 12/7/20 remove the plugin argument and instantiate drawables with plugins directly
    public synchronized void attach(@NotNull Drawable drawable, @NotNull Plugin plugin) throws RectOutOfBoundsException, NodeNotFoundException {
        if (!Rect.fits(getZeroRect(), drawable.getRelativeRect())) {
            throw new RectOutOfBoundsException("Drawable does not fit within the container");
        }

        // Attach
        getBranch().newNode(drawable);

        // Register any events
        AccelaAPI.getPluginManager().registerEvents(drawable, drawable.getPlugin(), drawable.getChannel());

        // Focus
        // FIXME: 11/25/20 disabled for now, same in wm
        //getWindowManager().broadcastEvent(new ActivationEvent(getPlugin(), drawable.getIdentifier()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void detach(@NotNull Drawable drawable) throws NodeNotFoundException {
        // Get the rect before detaching, we're going to need it later
        Rect rect = drawable.getAbsoluteRect();

        // Kill node
        Node node = DrawableTree.getNode(drawable);
        if (node != null) node.kill();

        // Unregister events
        AccelaAPI.getPluginManager().unregisterEvents(drawable);

        // Redraw the now empty rect
        paint(rect);

        // Attempt to grab a new drawable, if any are still attached.
        // If yes, then focus that one. If it's null, then focus null instead to show the change.
        List<Node> nodes = getBranch().getChildNodeList();
        DrawableIdentifier focusIdentifier = nodes.size() > 0 ? nodes.get(0).getDrawable().getIdentifier() : null;
        getWindowManager().broadcastEvent(new ActivationEvent(getPlugin(), focusIdentifier));
    }

    @Override
    public void paint(@NotNull Rect rect) throws NodeNotFoundException {
        // Attempt to calculate an intersecting Rect, and ensure it's within the boundaries of this container
        final Rect containerBounds = getZeroRect();
        final Rect targetRect = Rect.intersection(containerBounds, rect);
        if (targetRect == null) throw new IllegalStateException(
                "\n" + rect + "\n is outside the container boundaries \n" + containerBounds);

        getAnyContainer().paint(rect.startPointAddition(getRelativeRect().getStartPoint()));
    }

    @Override
    protected @NotNull Canvas getCanvas() throws NodeNotFoundException {
        return getCanvas(getZeroRect());
    }

    @Override
    protected @NotNull Canvas getCanvas(@NotNull Rect rect) throws NodeNotFoundException {
        // Attempt to calculate an intersecting Rect, and ensure it's within the boundaries of this container
        final Rect containerBounds = getZeroRect();
        final Rect targetRect = Rect.intersection(containerBounds, rect);
        if (targetRect == null) throw new IllegalStateException(
                rect + " is outside the container boundaries " + containerBounds);

        // Create a new canvas
        Canvas canvas = new Canvas(targetRect.getSize());

        // Get all drawables within the rectangle
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

            // Paint the Canvas
            Canvas.paintHard(canvas, Point.ZERO, drawable.getCanvas(), drawableRect.getStartPoint());
        }
        return canvas;
    }

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
