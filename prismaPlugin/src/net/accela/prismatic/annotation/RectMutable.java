package net.accela.prismatic.annotation;

import net.accela.prismatic.ui.geometry.Rect;
import org.jetbrains.annotations.NotNull;

public interface RectMutable extends RectReadable, PointMutable, SizeMutable {
    /**
     * @param rect The size and relative position of this.
     * @see RectMutable#setAbsoluteRect(Rect)
     */
    void setRelativeRect(@NotNull Rect rect);

    /**
     * @param rect The size and absolute position of this.
     * @see RectMutable#setRelativeRect(Rect)
     */
    default void setAbsoluteRect(@NotNull Rect rect) {
        Rect absCurrentRect = getAbsoluteRect();
        Rect relNewRect = new Rect(
                rect.getMinX() - absCurrentRect.getMinX(),
                rect.getMinY() - absCurrentRect.getMinY(),
                rect.getWidth(),
                rect.getHeight()
        );
        setRelativeRect(relNewRect);
    }
}
