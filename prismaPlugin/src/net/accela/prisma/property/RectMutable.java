package net.accela.prisma.property;

import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Rect;
import org.jetbrains.annotations.NotNull;

public interface RectMutable extends RectReadable, PointMutable, SizeMutable {
    /**
     * @param rect The size and relative position of this.
     * @see RectMutable#setAbsoluteRect(Rect)
     */
    void setRelativeRect(@NotNull Rect rect) throws NodeNotFoundException;

    /**
     * @param rect The size and absolute position of this.
     * @see RectMutable#setRelativeRect(Rect)
     */
    default void setAbsoluteRect(@NotNull Rect rect) throws NodeNotFoundException {
        Rect absCurrentRect = getAbsoluteRect();
        // todo move into combine method
        Rect relNewRect = new Rect(
                rect.getMinX() - absCurrentRect.getMinX(),
                rect.getMinY() - absCurrentRect.getMinY(),
                rect.getWidth(),
                rect.getHeight()
        );
        setRelativeRect(relNewRect);
    }
}
