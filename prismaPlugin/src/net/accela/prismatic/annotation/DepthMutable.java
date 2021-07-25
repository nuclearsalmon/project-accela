package net.accela.prismatic.annotation;

public interface DepthMutable {
    /**
     * @param relDepth The relative depth.
     */
    void setRelativeDepth(int relDepth);

    /**
     * @param absDepth The absolute depth.
     */
    void setAbsoluteDepth(int absDepth);
}
