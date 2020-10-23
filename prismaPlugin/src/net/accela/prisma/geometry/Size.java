package net.accela.prisma.geometry;

import org.jetbrains.annotations.NotNull;

public final class Size {
    final int width, height;

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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCapacity() {
        return width * height;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Size)) {
            return false;
        } else {
            Size d = (Size) obj;
            return this.width == d.width && this.height == d.height;
        }
    }

    public int hashCode() {
        int sum = this.width + this.height;
        return sum * (sum + 1) / 2 + this.width;
    }

    public String toString() {
        return this.getClass().getName() + "[width=" + this.width + ",height=" + this.height + "]";
    }
}
