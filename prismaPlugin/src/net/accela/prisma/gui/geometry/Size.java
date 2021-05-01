package net.accela.prisma.gui.geometry;

import net.accela.prisma.gui.geometry.exception.RectOutOfBoundsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Represents the size (width and height) of something.
 * Minimum allowed value is 1, the size cannot be 0 in any dimension.
 */
public class Size {
    @Range(from = 1, to = Integer.MAX_VALUE)
    private final int width, height;

    //
    // Constructors
    //

    public Size() {
        this(1, 1);
    }

    public Size(@Range(from = 1, to = Integer.MAX_VALUE) int side) throws IndexOutOfBoundsException {
        this(side, side);
    }

    public Size(@NotNull Size size) throws IndexOutOfBoundsException {
        this(size.getWidth(), size.getHeight());
    }

    public Size(@Range(from = 1, to = Integer.MAX_VALUE) int width,
                @Range(from = 1, to = Integer.MAX_VALUE) int height) throws IndexOutOfBoundsException {
        //noinspection ConstantConditions
        if (width < 1 || height < 1) {
            throw new RectOutOfBoundsException("Bad dimensions \n" + this);
        }

        this.width = width;
        this.height = height;
    }

    //
    // Getters
    //

    /**
     * @return The width
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public final int getWidth() {
        return width;
    }

    /**
     * @return The height
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public final int getHeight() {
        return height;
    }

    /**
     * @return The width multiplied with the height
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
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
