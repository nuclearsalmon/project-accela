package net.accela.prisma.gui;

import net.accela.prisma.annotation.Container;
import net.accela.prisma.gui.drawabletree.Branch;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import org.jetbrains.annotations.NotNull;

public abstract class DrawableContainer extends Drawable implements Container {
    //
    // Node methods
    //

    public @NotNull Branch getBranch() throws NodeNotFoundException {
        return (Branch) super.findNode();
    }
}
