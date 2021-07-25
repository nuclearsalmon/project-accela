package net.accela.prismatic;

import net.accela.prismatic.annotation.ItemPainter;
import net.accela.prismatic.ui.geometry.exception.RectOutOfBoundsException;
import net.accela.server.AccelaAPI;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A package-private representation of common features that are to be
 * expected from a container for {@link Drawable}'s.
 * <p>
 * Implemented in {@link DrawableContainer} and {@link Prismatic}.
 */
interface ContainerInterface extends ItemPainter {
    //
    // Attachment and detachment methods
    //

    /**
     * Attaches multiple {@link Drawable}s to this {@link ContainerInterface}
     *
     * @param drawables The {@link Drawable}s to attach
     * @throws RectOutOfBoundsException If the rect is out of bounds or otherwise invalid
     */
    default void attachAll(final @NotNull Drawable... drawables) throws IOException {
        for (Drawable drawable : drawables) {
            attach(drawable);
        }
    }

    /**
     * Attaches a {@link Drawable} to this {@link ContainerInterface}
     *
     * @param drawable The {@link Drawable} to attach
     * @throws RectOutOfBoundsException If the rect is out of bounds or otherwise invalid
     */
    void attach(final @NotNull Drawable drawable) throws RectOutOfBoundsException, IOException;

    /**
     * Detaches a {@link Drawable} from this {@link ContainerInterface}
     *
     * @param drawable The {@link Drawable} to detach
     */
    void detach(@NotNull Drawable drawable) throws IOException;


    //
    // Positioning
    //

    /**
     * @param relDepth The relative depth.
     */
    void setRelativeDepth(int relDepth, @NotNull Drawable drawable);

    /**
     * @param absDepth The absolute depth.
     */
    void setAbsoluteDepth(int absDepth, @NotNull Drawable drawable);

    /**
     * @param drawable The {@link Drawable} to be focused.
     */
    void setFocusedDrawable(@NotNull Drawable drawable);

    /**
     * @return The absolute depth.
     */
    int getDepth(@NotNull Drawable drawable);


    //
    // Events
    //

    default void registerDrawableEvents(@NotNull Drawable drawable) {
        AccelaAPI.getPluginManager().registerEvents(drawable, drawable.getPlugin(), drawable.getChannel());
    }

    default void unregisterDrawableEvents(@NotNull Drawable drawable) {
        AccelaAPI.getPluginManager().unregisterEvents(drawable);
    }
}
