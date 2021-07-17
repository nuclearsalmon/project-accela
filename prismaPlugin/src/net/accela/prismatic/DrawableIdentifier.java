package net.accela.prismatic;

import org.jetbrains.annotations.NotNull;

/**
 * A security feature to be used as an ID for identifying {@link Drawable}s
 * without directly referencing the actual {@link Drawable}s.
 * <p>
 * The {@link Drawable}s that this {@link DrawableIdentifier} represents can only be retrieved
 * by classes in the same package.
 */
public class DrawableIdentifier {
    private final @NotNull Drawable drawable;

    public DrawableIdentifier(@NotNull Drawable drawable) {
        this.drawable = drawable;
    }

    /**
     * Returns the {@link Drawable} this identifier represents.
     * For security reasons, this method has to be package-private.
     *
     * @return The {@link Drawable} this identifier represents.
     */
    @NotNull Drawable getDrawable() {
        return drawable;
    }
}
