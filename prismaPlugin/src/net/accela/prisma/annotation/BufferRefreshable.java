package net.accela.prisma.annotation;

import java.io.IOException;

/**
 * Anything with a back and front buffer that can be refreshed.
 */
public interface BufferRefreshable {
    /**
     * Refreshes the contents of this {@link BufferRefreshable}.
     */
    void refresh() throws IOException;
}
