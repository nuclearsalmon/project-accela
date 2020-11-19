package net.accela.prisma;

import net.accela.prisma.exception.DeadWMException;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Containers contain {@link Drawable}'s.
 */
public interface Container {
    /**
     * Attaches a {@link Drawable} to this {@link Container}
     *
     * @param drawable The {@link Drawable} to attach
     * @throws RectOutOfBoundsException when the rect is invalid
     */
    void attach(@NotNull Drawable drawable, @NotNull Plugin plugin) throws RectOutOfBoundsException, DeadWMException, NodeNotFoundException;

    /**
     * Detaches a {@link Drawable} from this {@link Container}
     */
    void detach(@NotNull Drawable drawable) throws NodeNotFoundException;

    /**
     * Draws the contents of the requested {@link Drawable}, as well as any intersecting {@link Drawable}'s.
     */
    void draw(@NotNull Drawable drawable) throws DeadWMException, NodeNotFoundException;

    /**
     * Draws the contents of the requested (absolute) rect.
     */
    void draw(@NotNull Rect rect) throws DeadWMException, NodeNotFoundException;

    /**
     * @return A {@link Rect} representing the relative size and position of this {@link Drawable}.
     * Relative, in this case, means from the perspective of the {@link Container} of this {@link Drawable}.
     */
    @NotNull Rect getRelativeRect();

    /**
     * @return A {@link Rect} representing the absolute size and position of this {@link Drawable}.
     * Absolute, in this case, means from the perspective of {@link PrismaWM}.
     * <p>
     * In practise, this means recursively adding the return values of getAbsoluteRect
     * from all the {@link Container}s that are attached to each other,
     * resulting in the actual terminal {@link Point} of this {@link Drawable}.
     */
    @NotNull
    Rect getAbsoluteRect() throws NodeNotFoundException;

    /**
     * Validates if a rect is allowed or not for the drawable.
     *
     * @param drawable The drawable to approve for
     * @param rect     A relative rect to validate
     * @throws RectOutOfBoundsException If the request is denied
     */
    void tryRect(@NotNull Drawable drawable, @NotNull Rect rect) throws RectOutOfBoundsException;
}
