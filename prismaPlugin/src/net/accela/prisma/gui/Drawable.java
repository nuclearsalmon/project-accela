package net.accela.prisma.gui;

import net.accela.prisma.Prismatic;
import net.accela.prisma.annotation.Container;
import net.accela.prisma.annotation.ItemPainter;
import net.accela.prisma.annotation.RectReadable;
import net.accela.prisma.annotation.SelfPainter;
import net.accela.prisma.event.ActivationEvent;
import net.accela.prisma.gui.drawabletree.Branch;
import net.accela.prisma.gui.drawabletree.DrawableTree;
import net.accela.prisma.gui.drawabletree.Node;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Point;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.text.TextGrid;
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

    public Drawable() {
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

    public @NotNull Point getCursorRestingPoint() throws NodeNotFoundException {
        return getAbsoluteRect().getStartPoint();
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

    @NotNull
    public TextGrid getTextGrid(@NotNull Rect rect) throws NodeNotFoundException {
        return getTextGrid().getCrop(rect);
    }

    protected final void paintAfterGeometryChange(@NotNull Rect oldRect, @NotNull Rect newRect) {
        try {
            ItemPainter painter = findPainter();
            if (Rect.intersects(oldRect, newRect)) {
                painter.paint(Rect.combine(oldRect, newRect));
            } else {
                painter.paint(oldRect);
                painter.paint(newRect);
            }
        } catch (NodeNotFoundException | IOException ignored) {
        }
    }

    //
    // Container methods
    //

    /**
     * Attach this {@link Drawable} to the provided {@link net.accela.prisma.Prismatic}, using the provided {@link Plugin}.
     * <br>
     * Synonymous to {@link net.accela.prisma.Prismatic#attach(Drawable, Plugin)}.
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
        Node selfNode = findNode();
        Branch parentNode = selfNode.getParent();
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
        Node selfNode = findNode();
        Branch parentNode = selfNode.getParent();
        if (parentNode != null) return parentNode.getDrawable();
        else return null;
    }

    public final @NotNull ItemPainter findPainter() throws NodeNotFoundException {
        Node selfNode = findNode();
        Branch parentNode = selfNode.getParent();

        return parentNode == null ? selfNode.getWindowManager() : parentNode.getDrawable();
    }

    //
    // Positioning
    //

    /**
     * @return The size and absolute position of this {@link Drawable}.
     */
    @NotNull
    public final Rect getAbsoluteRect() throws NodeNotFoundException {
        Rect thisRect = getRelativeRect();

        DrawableContainer parentContainer = findParentContainer();
        if (parentContainer != null) {
            Rect absParentRect = parentContainer.getAbsoluteRect();
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
        Point thisPoint = getRelativePoint();

        DrawableContainer parentContainer = findParentContainer();
        if (parentContainer != null) {
            Point absParentPoint = parentContainer.getAbsolutePoint();
            return new Point(
                    absParentPoint.getX() + thisPoint.getX(),
                    absParentPoint.getY() + thisPoint.getY()
            );
        } else {
            return thisPoint;
        }
    }

    //
    // Events
    //

    /**
     * A default/built-in reaction to {@link ActivationEvent}s.
     * It is used to track whether or not this {@link Drawable} is active or inactive.<br>
     * Feel free to override it with custom code if so is desired.
     *
     * @param event An {@link ActivationEvent}.
     * @throws NodeNotFoundException If self node isn't found.
     * @see EventHandler
     * @see net.accela.server.event.Event
     * @see ActivationEvent
     */
    @EventHandler
    protected void onActivation(ActivationEvent event) throws NodeNotFoundException, IOException {
        DrawableIdentifier identifier = event.getTarget();
        isActive = identifier == this.identifier || identifier == null;
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        try {
            return super.toString() + " : \nrect=" + getRelativeRect();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}