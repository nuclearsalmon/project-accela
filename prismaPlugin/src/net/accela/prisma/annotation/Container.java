package net.accela.prisma.annotation;

import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.exception.RectOutOfBoundsException;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Containers contain {@link Drawable}'s.
 */
public interface Container extends ItemPainter {
    /*
    //
    // Node methods
    //

    @NotNull Branch getBranch() throws NodeNotFoundException;
     */

    //
    // Attachment and detachment methods
    //

    /**
     * Attaches a {@link Drawable} to this {@link Container}
     *
     * @param drawable The {@link Drawable} to attach
     * @throws RectOutOfBoundsException when the rect is invalid
     */
    void attach(@NotNull Drawable drawable, @NotNull Plugin plugin) throws RectOutOfBoundsException, NodeNotFoundException, IOException;

    /**
     * Detaches a {@link Drawable} from this {@link Container}
     */
    void detach(@NotNull Drawable drawable) throws NodeNotFoundException, IOException;
}
