package net.accela.prisma.geometry;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the size (width and height) of something.
 * Minimum allowed value is 1, the size cannot be 0 in any dimension.
 */
public class Size {
    private final int width, height;

    //
    // Constructors
    //

    public Size() {
        this(1, 1);
    }

    public Size(int side) throws IndexOutOfBoundsException {
        this(side, side);
    }

    public Size(@NotNull Size size) {
        this.width = size.width;
        this.height = size.height;
    }

    public Size(int width, int height) throws IndexOutOfBoundsException {
        if (width < 1 || height < 1) throw new IndexOutOfBoundsException();

        this.width = width;
        this.height = height;
    }

    //
    // Getters
    //

    /**
     * @return The width
     */
    public final int getWidth() {
        return width;
    }

    /**
     * @return The height
     */
    public final int getHeight() {
        return height;
    }

    /**
     * @return The width multiplied with the height
     */
    public final int getCapacity() {
        return width * height;
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Size)) {
            return false;
        } else {
            Size d = (Size) obj;
            return this.width == d.width && this.height == d.height;
        }
    }

    @Override
    public int hashCode() {
        int sum = this.width + this.height;
        return sum * (sum + 1) / 2 + this.width;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[width=" + this.width + ",height=" + this.height + "]";
    }
}
