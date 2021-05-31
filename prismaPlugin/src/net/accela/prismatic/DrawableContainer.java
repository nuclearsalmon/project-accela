package net.accela.prismatic;

import net.accela.prismatic.event.FocusEvent;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.geometry.exception.RectOutOfBoundsException;
import net.accela.server.AccelaAPI;
import net.accela.server.event.EventHandler;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class DrawableContainer extends Drawable implements ContainerInterface {
    protected final List<@NotNull Drawable> childDrawables = new LinkedList<>();
    protected @Nullable DrawableIdentifier focusTarget;

    /**
     * Whether new drawables are inserted on top
     */
    protected boolean insertNewDrawablesOnTop = true;

    //
    // Constructor
    //

    public DrawableContainer(@NotNull Rect rect, @NotNull Plugin plugin) {
        super(rect, plugin);
    }

    //
    // Self Attachment

    @Override
    synchronized void setAttached(@Nullable ContainerInterface parent) {
        super.setAttached(parent);
        synchronized (childDrawables) {
            if (isAttached()) {
                for (final Drawable drawable : childDrawables) {
                    // Register any events
                    registerDrawableEvents(drawable);
                }
            } else {
                for (final Drawable drawable : childDrawables) {
                    // Unregister any events
                    unregisterDrawableEvents(drawable);
                }
            }
        }
    }


    //
    // Child Drawable Attachment
    //

    public synchronized void attachAll(final @NotNull Drawable[] drawables) {
        for (Drawable drawable : drawables) {
            attach(drawable);
        }
    }

    public synchronized void attach(final @NotNull Drawable drawable) throws RectOutOfBoundsException {
        synchronized (this) {
            // Confirm attachment
            if (childDrawables.contains(drawable)) {
                throw new IllegalArgumentException("Drawable is already attached to this container");
            }

            if (Main.DBG_RESPECT_CONTAINER_BOUNDS
                    && !Rect.contains(getZeroRect(), drawable.getRelativeRect())) {
                throw new RectOutOfBoundsException("Drawable does not fit within the container");
            }

            // Attach
            synchronized (childDrawables) {
                childDrawables.add(getInsertionIndex(drawable), drawable);
            }
            drawable.setAttached(this);

            if (isAttached()) {
                // Register any events
                registerDrawableEvents(drawable);
            }

            // Focus
            setFocusedDrawable(drawable);
        }
    }

    public synchronized void detachAll(final @NotNull Drawable[] drawables) throws IOException {
        for (Drawable drawable : drawables) {
            detach(drawable);
        }
    }

    @Override
    public synchronized void detach(@NotNull Drawable drawable) throws IOException {
        synchronized (this) {
            // Confirm attachment
            if (!childDrawables.contains(drawable)) {
                throw new IllegalArgumentException("Drawable is not attached to this container");
            }

            // Get the rect before detaching, we're going to need it later
            Rect rect = drawable.getAbsoluteRect();

            // Detach
            synchronized (childDrawables) {
                childDrawables.remove(drawable);
            }
            drawable.setAttached(null);

            // Unregister events
            unregisterDrawableEvents(drawable);

            // Focus
            Drawable newFocusedDrawable = childDrawables.size() > 0 ? childDrawables.get(0) : null;
            setFocusedDrawable(newFocusedDrawable);

            // Redraw the now empty rect
            paint(rect);
        }
    }

    protected int getInsertionIndex(@NotNull Drawable drawable) {
        if (insertNewDrawablesOnTop) {
            return childDrawables.size();
        } else {
            return 0;
        }
    }

    //
    // Focusing
    //

    /**
     * @param drawable The {@link Drawable} to be focused.
     */
    protected void setFocusedDrawable(@Nullable Drawable drawable) {
        if (!childDrawables.contains(drawable)) {
            throw new IllegalArgumentException("Drawable not attached to this Container!");
        }

        Prismatic prismatic = getPrismatic();
        if (prismatic != null) {
            // Establish identifier and handle null condition
            DrawableIdentifier drawableIdentifier = drawable == null ? null : drawable.identifier;

            // Push focus event
            AccelaAPI.getPluginManager().callEvent(
                    new FocusEvent(getPlugin(), drawableIdentifier),
                    prismatic.getBroadcastChannel()
            );
        }
    }

    //
    // Calculations
    //

    /**
     * @param rect The {@link Rect} to look for {@link Drawable}s within. Relative.
     * @return All {@link Drawable}s that are situated within the {@link Rect} provided
     */
    public @NotNull List<@NotNull Drawable> getIntersectingDrawables(@NotNull Rect rect) {
        synchronized (this.childDrawables) {
            List<Drawable> drawables = new ArrayList<>();
            for (Drawable drawable : this.childDrawables) {
                if (rect.intersects(drawable.getRelativeRect())) {
                    drawables.add(drawable);
                }
            }
            return drawables;
        }
    }

    /**
     * @return A list of {@link Drawable}s that are immediately connected to this SecureTree
     */
    public @NotNull List<@NotNull Drawable> getImmediateChildDrawables() {
        return List.copyOf(childDrawables);
    }

    public @NotNull List<@NotNull Drawable> getAllChildDrawables() {
        final List<Drawable> allChildDrawables = new ArrayList<>();
        for (final Drawable drawable : childDrawables) {
            // Add the drawable
            allChildDrawables.add(drawable);

            // Check if it may contain child Drawables. If yes, then add those to the list as well.
            if (drawable instanceof DrawableContainer) {
                DrawableContainer drawableContainer = (DrawableContainer) drawable;
                // Add child drawables to the list
                allChildDrawables.addAll(drawableContainer.getAllChildDrawables());
            }
        }
        return allChildDrawables;
    }

    public @NotNull List<@NotNull Drawable> getAllChildDrawablesAndSelf() {
        final List<Drawable> allChildDrawablesAndSelf = getAllChildDrawables();
        allChildDrawablesAndSelf.add(this);
        return allChildDrawablesAndSelf;
    }

    //
    // Events
    //

    @EventHandler
    private void onFocus(FocusEvent event) {
        this.focusTarget = event.getTarget();
    }
}
