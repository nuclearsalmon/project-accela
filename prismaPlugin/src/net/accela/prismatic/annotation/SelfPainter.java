package net.accela.prismatic.annotation;

import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;

import java.io.IOException;

/**
 * Anything that can paint or draw itself
 */
public interface SelfPainter {
    /**
     * Draws itself
     */
    void paint() throws NodeNotFoundException, IOException;
}
