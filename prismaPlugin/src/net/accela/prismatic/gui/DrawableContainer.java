package net.accela.prismatic.gui;

import net.accela.prismatic.annotation.Container;
import net.accela.prismatic.gui.drawabletree.Branch;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Rect;
import org.jetbrains.annotations.NotNull;

public abstract class DrawableContainer extends Drawable implements Container {
    //
    // Constructor
    //

    public DrawableContainer(@NotNull Rect rect) {
        super(rect);
    }

    //
    // Node methods
    //

    public @NotNull Branch getBranch() throws NodeNotFoundException {
        return (Branch) super.findNode();
    }
}
