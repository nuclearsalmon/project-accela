package net.accela.prismatic;

import net.accela.prismatic.annotation.ItemPainter;
import net.accela.prismatic.annotation.RectReadable;
import net.accela.prismatic.annotation.SelfPainter;
import net.accela.prismatic.event.FocusEvent;
import net.accela.prismatic.ui.geometry.Point;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.geometry.Size;
import net.accela.prismatic.ui.text.TextGrid;
import net.accela.server.event.EventChannel;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public abstract class Drawable implements RectReadable, Listener, SelfPainter {
    /**
     * The EventChannel for this drawable.
     */
    protected final EventChannel channel = new EventChannel();

    /**
     * The ID for this Drawable
     */
    protected final DrawableIdentifier identifier;

    /**
     * If the Drawable is marked as active, it will be selected to receive Events. It will be "focused".
     * It can still receive Events when inactive, but is less likely to.
     */
    protected boolean isEventActive = false;

    private @NotNull Rect rect;

    private boolean attached;
    private @Nullable ContainerInterface parentContainer;
    public final @NotNull Plugin plugin;
    private @Nullable DrawableContainer parentDrawable;

    public Drawable(@NotNull Rect rect, @NotNull Plugin plugin) {
        this.rect = rect;
        this.plugin = plugin;
        this.identifier = new DrawableIdentifier(this);
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
    public boolean isEventActive() {
        return isEventActive;
    }

    public @NotNull Point getAbsoluteCursorRestingPoint() {
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

    public abstract @NotNull CursorMode getCursorMode();

    @Deprecated
    public abstract boolean cursorEnabled();

    public abstract boolean transparent();


    //
    // Painting
    //

    public final void paint() throws IOException {
        ContainerInterface parent = getParentContainer();
        if (parent != null) parent.paint(this);
    }

    @NotNull
    public abstract TextGrid getTextGrid();

    @NotNull
    public TextGrid getTextGrid(@NotNull Rect rect) {
        return getTextGrid().getCrop(rect);
    }


    //
    // Hierarchy
    //

    synchronized void attachSelf(@Nullable ContainerInterface parentContainer) {
        this.parentContainer = parentContainer;
        this.attached = this.parentContainer == null;
        if (parentContainer instanceof DrawableContainer) {
            this.parentDrawable = (DrawableContainer) parentContainer;
        }
    }

    synchronized void detachSelf() {
        attachSelf(null);
    }

    public boolean isAttached() {
        return attached;
    }

    public final @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Nullable
    public Prismatic getPrismatic() {
        ContainerInterface parent = getParentContainer();
        if (parent instanceof Prismatic) return (Prismatic) parent;
        else {
            Drawable rootDrawable = getRootDrawable(false);
            if (rootDrawable == null) return null;
            else return rootDrawable.getPrismatic();
        }
    }

    @Nullable
    public ContainerInterface getParentContainer() {
        return parentContainer;
    }

    @Nullable
    public DrawableContainer getParentDrawable() {
        return parentDrawable;
    }

    @Nullable
    public Drawable getRootDrawable(boolean includeSelf) {
        if (parentDrawable == null) return (includeSelf ? this : null);
        else return parentDrawable.getRootDrawable(includeSelf);
    }

    @Nullable
    public Drawable getRootDrawableExclusive() {
        return getRootDrawable(false);
    }

    @NotNull
    public Drawable getRootDrawableInclusive() {
        return Objects.requireNonNull(getRootDrawable(true));
    }


    //
    // Size and positioning
    //

    /**
     * @return The size and absolute position of this {@link Drawable}.
     */
    @NotNull
    public final Rect getAbsoluteRect() {
        final Rect thisRect = getRelativeRect();

        final ContainerInterface parentContainer = getParentContainer();
        if (parentContainer instanceof DrawableContainer) {
            final Rect absParentRect = ((DrawableContainer) parentContainer).getAbsoluteRect();
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
    public final @NotNull Point getAbsolutePoint() {
        final Point thisPoint = getRelativePoint();

        final ContainerInterface parentContainer = getParentContainer();
        if (parentContainer instanceof DrawableContainer) {
            final Point absParentPoint = ((DrawableContainer) parentContainer).getAbsolutePoint();
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
    public final @NotNull Rect getRelativeRect() {
        return rect;
    }

    /**
     * @return This {@link Drawable}'s relative position.
     */
    @Override
    public final @NotNull Point getRelativePoint() {
        return rect.getStartPoint();
    }

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
                ItemPainter painter = getParentContainer();
                if (painter == null) return;

                if (Rect.intersects(oldRect, rect)) {
                    painter.paint(Rect.combine(oldRect, rect));
                } else {
                    painter.paint(oldRect);
                    painter.paint(rect);
                }
            } catch (IOException ignored) {
            }
        }
    }


    //
    // Events
    //

    /**
     * Used for performing actions before painting (such as resizing buffers)
     *
     * @param oldSize The old size.
     * @param newSize The new size.
     */
    protected void onResizeBeforePainting(@NotNull Size oldSize, @NotNull Size newSize) {
    }

    /**
     * A default reaction to {@link FocusEvent}s.
     * It is used to track whether or not this {@link Drawable} is active or inactive.<br>
     *
     * @param event An {@link FocusEvent}.
     * @see EventHandler
     * @see net.accela.server.event.Event
     * @see FocusEvent
     */
    @EventHandler
    private void onFocus(FocusEvent event) {
        DrawableIdentifier identifier = event.getTarget();
        this.isEventActive = identifier == this.identifier || identifier == null;
    }


    //
    // Object overrides
    //

    @Override
    public String toString() {
        return super.toString() + "{rect=" + getRelativeRect() + "}";
    }
}
