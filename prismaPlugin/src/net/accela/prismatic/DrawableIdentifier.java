package net.accela.prismatic;

import org.jetbrains.annotations.NotNull;

/**
 * A security feature to be used as an ID for identifying {@link Drawable}s
 * without publicly referencing the {@link Drawable} objects.
 * <p>
 * The {@link Drawable} this represents can be accessed by classes in the same package,
 * by using a package private method.
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
    Drawable getDrawable() {
        return drawable;
    }
}
