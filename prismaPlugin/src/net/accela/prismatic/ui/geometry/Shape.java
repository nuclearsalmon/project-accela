package net.accela.prismatic.ui.geometry;

import org.jetbrains.annotations.NotNull;

public interface Shape {
    /**
     * @return The bounds of this {@link Shape} when represented as a {@link Rect}.
     */
    @NotNull Rect getBounds();

    /**
     * @param point The {@link Point} to compare with.
     * @return True if the provided {@link Point} fits inside this {@link Shape}.
     */
    boolean contains(@NotNull Point point);

    /**
     * @param rect The {@link Rect} to compare with.
     * @return True if the provided {@link Rect} fits inside this {@link Shape}.
     */
    boolean contains(@NotNull Rect rect);

    /**
     * @param rect The {@link Rect} to compare with.
     * @return True if the provided {@link Rect} intersects with this {@link Shape}.
     */
    boolean intersects(@NotNull Rect rect);
}
