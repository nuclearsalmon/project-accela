package net.accela.prisma;

import net.accela.prisma.drawable.property.Container;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.util.drawabletree.Branch;
import org.jetbrains.annotations.NotNull;

public abstract class DrawableContainer extends Drawable implements Container {
    //
    // Node methods
    //

    @Override
    public @NotNull Branch getBranch() throws NodeNotFoundException {
        return (Branch) super.findNode();
    }
}
