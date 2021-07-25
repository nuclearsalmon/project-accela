package net.accela.prismatic.annotation;

import java.io.IOException;

/**
 * Anything that can paint or draw itself
 */
public interface SelfPainter {
    /**
     * Draws itself
     */
    void render() throws IOException;
}
