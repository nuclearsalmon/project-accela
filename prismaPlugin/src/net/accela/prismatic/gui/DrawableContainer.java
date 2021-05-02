package net.accela.prismatic.gui;

import net.accela.prismatic.annotation.Container;
import net.accela.prismatic.gui.drawabletree.Branch;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import org.jetbrains.annotations.NotNull;

public abstract class DrawableContainer extends Drawable implements Container {
    //
    // Node methods
    //

    public @NotNull Branch getBranch() throws NodeNotFoundException {
        return (Branch) super.findNode();
    }
}
