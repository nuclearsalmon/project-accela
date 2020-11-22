package net.accela.prisma;

import net.accela.prisma.event.ActivationEvent;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.util.Canvas;
import net.accela.prisma.util.drawabletree.Branch;
import net.accela.prisma.util.drawabletree.DrawableTree;
import net.accela.prisma.util.drawabletree.Node;
import net.accela.server.event.EventChannel;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Drawable implements Listener {
    /**
     * The EventChannel for this drawable
     */
    protected final EventChannel eventChannel = new EventChannel();

    /**
     * The ID for this Drawable
     */
    protected final DrawableIdentifier identifier = new DrawableIdentifier() {
    };

    /**
     * The rectangle bounds of this drawable
     */
    @NotNull
    private Rect rect;

    /**
     * If the Drawable is marked as active, it will be selected to receive Events.
     * It can still receive Events when inactive, of course, but it's less likely to.
     */
    protected boolean isActive = false;

    public Drawable(@NotNull Rect rect) {
        this.rect = rect;
    }

    public void setRect(@NotNull Rect newRect) throws NodeNotFoundException {
        Rect oldRect = this.rect;
        this.rect = newRect;
        getAnyContainer().paint(Rect.combine(oldRect, newRect));
    }

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
        return eventChannel;
    }

    /**
     * Attach this {@link Drawable} to the provided {@link PrismaWM} windowManager, using the provided {@link Plugin}.
     * <br>
     * Synonymous to {@link PrismaWM#attach(Drawable, Plugin)}.
     *
     * @param windowManager The {@link PrismaWM} windowManager to attach the {@link Drawable} to.
     * @param plugin        The {@link Plugin} to use when registering.
     */
    public final void attach(@NotNull PrismaWM windowManager, @NotNull Plugin plugin) throws NodeNotFoundException {
        windowManager.attach(this, plugin);
    }

    public final @NotNull Node getNode() throws NodeNotFoundException {
        Node selfNode = DrawableTree.getNode(this);
        if (selfNode == null) throw new NodeNotFoundException("SelfNode not found");
        else return selfNode;
    }

    /**
     * @return The {@link Plugin} that initialized this {@link Drawable}
     */
    public final @NotNull Plugin getPlugin() throws NodeNotFoundException {
        return getNode().getPlugin();
    }

    /**
     * @return The {@link PrismaWM} instance hosting this {@link Drawable}.
     */
    public final @NotNull PrismaWM getWindowManager() throws NodeNotFoundException {
        return getNode().getWindowManager();
    }

    public final @Nullable DrawableContainer getParentContainer() throws NodeNotFoundException {
        Node selfNode = getNode();
        Branch parentNode = selfNode.getParent();
        if (parentNode != null) return parentNode.getDrawable();
        else return null;
    }

    public final @NotNull Container getAnyContainer() throws NodeNotFoundException {
        Node selfNode = getNode();
        Branch parentNode = selfNode.getParent();
        if (parentNode != null) return parentNode.getDrawable();
        else return selfNode.getWindowManager();
    }

    /**
     * @return A {@link Rect} representing the size of this {@link Drawable},
     * with the values of {@link Rect#getStartPoint()} returning [0, 0].
     * The same result can be achieved using {@link Rect#zero()},
     * which is what this uses internally.
     */
    public final @NotNull Rect getZeroRect() {
        return getRelativeRect().zero();
    }

    /**
     * @return A {@link Rect} representing the relative size and position of this {@link Drawable}.
     * Relative, in this case, means from the perspective of the {@link Container} of this {@link Drawable}.
     */
    public final @NotNull Rect getRelativeRect() {
        return rect;
    }

    /**
     * @return A {@link Rect} representing the absolute size and position of this {@link Drawable}.
     * Absolute, in this case, means from the perspective of {@link PrismaWM}.
     * In practise, this means recursively adding the return values of {@link Container#getAbsoluteRect()}
     * from all the {@link Container}s that are attached to each other,
     * resulting in the actual terminal {@link Point} of this {@link Drawable}.
     */
    @NotNull
    public final Rect getAbsoluteRect() throws NodeNotFoundException {
        Rect containerRect = getAnyContainer().getAbsoluteRect();
        Rect thisRect = getRelativeRect();
        return new Rect(
                containerRect.getMinX() + thisRect.getMinX(),
                containerRect.getMinY() + thisRect.getMinY(),
                thisRect.getWidth(),
                thisRect.getHeight()
        );
    }

    /**
     * @return A {@link Size} representing this {@link Drawable}
     */
    public final Size getSize() {
        return getRelativeRect().getSize();
    }

    /**
     * @return The width of this {@link Drawable}
     */
    public final Integer getWidth() {
        return getRelativeRect().getWidth();
    }

    /**
     * @return The height of this {@link Drawable}
     */
    public final Integer getHeight() {
        return getRelativeRect().getHeight();
    }

    /**
     * See {@link Rect#getCapacity()}
     *
     * @return The capacity (width * height) of this {@link Drawable}
     */
    public final Integer getCapacity() {
        return getRelativeRect().getCapacity();
    }

    public boolean isActive() {
        return isActive;
    }

    public @NotNull Point getCursorRestingPoint() throws NodeNotFoundException {
        return getAbsoluteRect().getStartPoint();
    }

    public boolean cursorEnabled() {
        return false;
    }

    public final synchronized void paint() throws NodeNotFoundException {
        getAnyContainer().paint(this);
    }

    protected abstract @NotNull Canvas getCanvas() throws NodeNotFoundException;

    protected synchronized @NotNull Canvas getCanvas(@NotNull Rect rect) throws NodeNotFoundException {
        Canvas cutCanvas = new Canvas(rect.getSize());
        Canvas.paintHard(
                cutCanvas, rect.getStartPoint(),
                getCanvas(), Point.ZERO
        );
        return cutCanvas;
    }

    ///
    /// EVENTS
    ///

    @EventHandler
    protected void onActivation(ActivationEvent event) throws NodeNotFoundException {
        DrawableIdentifier identifier = event.getTarget();
        isActive = identifier == this.identifier || identifier == null;
        /*
        try {
            getPlugin().getLogger().log(Level.INFO, "ACTIVATION DETECTED!!!");
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
         */
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return super.toString() + " : \nrect=" + rect.toString();
    }
}
