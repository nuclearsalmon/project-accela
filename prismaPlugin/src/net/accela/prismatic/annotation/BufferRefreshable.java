package net.accela.prismatic.annotation;

/**
 * Anything with a back and front buffer that can be refreshed.
 */
public interface BufferRefreshable {
    /**
     * Refreshes the contents of this {@link BufferRefreshable}.
     */
    void refresh();
}
