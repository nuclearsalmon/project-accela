package net.accela.prismatic.gui;

import net.accela.prismatic.Prismatic;
import net.accela.prismatic.annotation.Container;
import net.accela.prismatic.annotation.ItemPainter;
import net.accela.prismatic.annotation.RectReadable;
import net.accela.prismatic.annotation.SelfPainter;
import net.accela.prismatic.event.ActivationEvent;
import net.accela.prismatic.gui.drawabletree.Branch;
import net.accela.prismatic.gui.drawabletree.DrawableTree;
import net.accela.prismatic.gui.drawabletree.Node;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Point;
import net.accela.prismatic.gui.geometry.Rect;
import net.accela.prismatic.gui.geometry.Size;
import net.accela.prismatic.gui.text.TextGrid;
import net.accela.server.event.EventChannel;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class Drawable implements RectReadable, Listener, SelfPainter {

    /**
     * The EventChannel for this drawable.
     */
    protected final EventChannel channel = new EventChannel();

    /**
     * The ID for this Drawable
     */
    protected final DrawableIdentifier identifier = new DrawableIdentifier() {
    };

    /**
     * If the Drawable is marked as active, it will be selected to receive Events. It will be "focused".
     * It can still receive Events when inactive, but is less likely to.
     */
    protected boolean isActive = false;

    /**
     * Cached node
     */
    private @Nullable Node cachedNode;

    private @NotNull Rect rect;

    public Drawable(@NotNull Rect rect) {
        this.rect = rect;
    }

    //
    // Properties and flags
    //

    /**
     * @return The {@link DrawableIdentifier} for this {@link Drawable}.
     */
    public final @NotNull DrawableIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * @return The channel this {@link Drawable} is listening in.
     */
    public final @NotNull EventChannel getChannel() {
        return channel;
    }

    /**
     * If the Drawable is marked as active, it will be selected to receive Events. It will be "focused".
     * It can still receive Events when inactive, but is less likely to.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isActive() {
        return isActive;
    }

    public @NotNull Point getAbsoluteCursorRestingPoint() throws NodeNotFoundException {
        final Rect absRect = getAbsoluteRect();
        int x = absRect.getMinX(), y = absRect.getMinY();
        // Ensure that it's not negative, if it is then correct it
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        return new Point(x, y);
    }

    /**
     * @return whether or not this {@link Drawable} is eligible for being focused.
     */
    public abstract boolean isFocusable();

    public enum CursorMode {
        NONE,
        TERMINAL_RENDERED,
        RENDERED
    }

    public abstract @NotNull CursorMode getCursorMode();

    @Deprecated
    public abstract boolean cursorEnabled();

    public abstract boolean transparent();

    //
    // Painting
    //

    public final void paint() throws NodeNotFoundException, IOException {
        findPainter().paint(this);
    }

    @NotNull
    public abstract TextGrid getTextGrid() throws NodeNotFoundException;

    // todo make abstract, don't favour certain implementations when equal
    @NotNull
    public TextGrid getTextGrid(@NotNull Rect rect) throws NodeNotFoundException {
        return getTextGrid().getCrop(rect);
    }

    //
    // Container methods
    //

    /**
     * Attach this {@link Drawable} to the provided {@link net.accela.prismatic.Prismatic}, using the provided {@link Plugin}.
     * <br>
     * Synonymous to {@link net.accela.prismatic.Prismatic#attach(Drawable, Plugin)}.
     *
     * @param container The {@link Container} to attach to.
     * @param plugin    The {@link Plugin} to use when registering.
     */
    public final void attachTo(@NotNull Prismatic container, @NotNull Plugin plugin) throws NodeNotFoundException, IOException {
        container.attach(this, plugin);
    }

    /**
     * Detach this {@link Drawable} from its {@link Container}.
     * <br>
     * Synonymous to {@link Container#detach(Drawable)}.
     */
    public final void detach() throws NodeNotFoundException, IOException {
        final Node selfNode = findNode();
        final Branch parentNode = selfNode.getParent();

        if (parentNode != null) parentNode.getDrawable().detach(this);
        else selfNode.getWindowManager().detach(this);
    }

    //
    // Node / Tree details
    //

    /**
     * @return the {@link Node} representing this {@link Drawable}.
     * @throws NodeNotFoundException If not found.
     */
    public final @NotNull Node findNode() throws NodeNotFoundException {
        // Retrieve node if needed
        if (this.cachedNode == null) {
            // Let's cache it
            // Will be null if not found. We'll check that later.
            this.cachedNode = DrawableTree.getNode(this);
        }

        // Confirm that the node is not null (null = not found), and that it's alive.
        if (!(cachedNode != null && this.cachedNode.isAlive())) {
            // Set it to null so that we don't maintain any references to dead nodes.
            this.cachedNode = null;
            throw new NodeNotFoundException("Self node not found");
        }

        // All went smoothly, let's return the node
        return this.cachedNode;
    }

    /**
     * @return The {@link Plugin} that initialized this {@link Drawable}.
     */
    public final @NotNull Plugin findPlugin() throws NodeNotFoundException {
        return findNode().getPlugin();
    }

    /**
     * @return The {@link Prismatic} instance hosting this {@link Drawable}.
     */
    public final @NotNull Prismatic findWindowManager() throws NodeNotFoundException {
        return findNode().getWindowManager();
    }

    public final @Nullable DrawableContainer findParentContainer() throws NodeNotFoundException {
        final Node selfNode = findNode();
        final Branch parentNode = selfNode.getParent();

        if (parentNode != null) return parentNode.getDrawable();
        else return null;
    }

    public final @NotNull ItemPainter findPainter() throws NodeNotFoundException {
        final Node selfNode = findNode();
        final Branch parentNode = selfNode.getParent();

        return parentNode == null ? selfNode.getWindowManager() : parentNode.getDrawable();
    }

    //
    // Size and position (reading)
    //

    /**
     * @return The size and absolute position of this {@link Drawable}.
     */
    @NotNull
    public final Rect getAbsoluteRect() throws NodeNotFoundException {
        final Rect thisRect = getRelativeRect();

        DrawableContainer parentContainer = findParentContainer();
        if (parentContainer != null) {
            final Rect absParentRect = parentContainer.getAbsoluteRect();
            return new Rect(
                    absParentRect.getMinX() + thisRect.getMinX(),
                    absParentRect.getMinY() + thisRect.getMinY(),
                    thisRect.getWidth(),
                    thisRect.getHeight()
            );
        } else {
            return thisRect;
        }
    }

    /**
     * @return This {@link Drawable}'s absolute position.
     */
    public final @NotNull Point getAbsolutePoint() throws NodeNotFoundException {
        final Point thisPoint = getRelativePoint();

        final DrawableContainer parentContainer = findParentContainer();
        if (parentContainer != null) {
            final Point absParentPoint = parentContainer.getAbsolutePoint();
            return new Point(
                    absParentPoint.getX() + thisPoint.getX(),
                    absParentPoint.getY() + thisPoint.getY()
            );
        } else {
            return thisPoint;
        }
    }

    /**
     * @return The size and relative position of this {@link Drawable}.
     */
    @Override
    public final @NotNull Rect getRelativeRect() throws NodeNotFoundException {
        return rect;
    }

    /**
     * @return This {@link Drawable}'s relative position.
     */
    @Override
    public final @NotNull Point getRelativePoint() {
        return rect.getStartPoint();
    }

    //
    // Size and position (writing)
    //

    protected void internalSetPoint(@NotNull Point point) {
        synchronized (this) {
            internalSetRect(new Rect(point, this.rect.getSize()));
        }
    }

    protected void internalSetSize(@NotNull Size size) {
        synchronized (this) {
            internalSetRect(new Rect(this.rect.getStartPoint(), size));
        }
    }

    protected void internalSetRect(@NotNull Rect rect) {
        synchronized (this) {
            final Rect oldRect = this.rect;
            this.rect = rect;

            // Perform actions before painting (such as resizing buffers)
            if (!oldRect.getSize().equals(rect.getSize())) {
                onResizeBeforePainting(oldRect.getSize(), rect.getSize());
            }

            // Repaint
            try {
                ItemPainter painter = findPainter();

                if (Rect.intersects(oldRect, rect)) {
                    painter.paint(Rect.combine(oldRect, rect));
                } else {
                    painter.paint(oldRect);
                    painter.paint(rect);
                }
            } catch (NodeNotFoundException | IOException ignored) {
            }
        }
    }

    /**
     * Used for performing actions before painting (such as resizing buffers)
     *
     * @param oldSize The old size.
     * @param newSize The new size.
     */
    protected void onResizeBeforePainting(@NotNull Size oldSize, @NotNull Size newSize) {
    }

    //
    // Events
    //

    /**
     * A default reaction to {@link ActivationEvent}s.
     * It is used to track whether or not this {@link Drawable} is active or inactive.<br>
     *
     * @param event An {@link ActivationEvent}.
     * @throws NodeNotFoundException If self node isn't found.
     * @see EventHandler
     * @see net.accela.server.event.Event
     * @see ActivationEvent
     */
    @EventHandler
    private void onActivation(ActivationEvent event) throws NodeNotFoundException {
        DrawableIdentifier identifier = event.getTarget();
        this.isActive = identifier == this.identifier || identifier == null;
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        try {
            return super.toString() + "{rect=" + getRelativeRect() + "}";
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
