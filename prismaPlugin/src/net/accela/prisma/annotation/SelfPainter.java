package net.accela.prisma.annotation;

import net.accela.prisma.gui.drawabletree.NodeNotFoundException;

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
