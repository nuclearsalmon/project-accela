package net.accela.prisma.drawable.property;

import net.accela.prisma.Drawable;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import net.accela.prisma.util.drawabletree.Branch;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Containers contain {@link Drawable}'s.
 */
public interface Container extends Painter {
    //
    // Node methods
    //

    @NotNull Branch getBranch() throws NodeNotFoundException;

    //
    // Attachment and detachment methods
    //

    /**
     * Attaches a {@link Drawable} to this {@link Container}
     *
     * @param drawable The {@link Drawable} to attach
     * @throws RectOutOfBoundsException when the rect is invalid
     */
    void attach(@NotNull Drawable drawable, @NotNull Plugin plugin) throws RectOutOfBoundsException, NodeNotFoundException;

    /**
     * Detaches a {@link Drawable} from this {@link Container}
     */
    void detach(@NotNull Drawable drawable) throws NodeNotFoundException;
}
