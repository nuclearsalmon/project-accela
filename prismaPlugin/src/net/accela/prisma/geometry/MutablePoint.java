package net.accela.prisma.geometry;

import org.jetbrains.annotations.NotNull;

public class MutablePoint extends Point {
    public MutablePoint() {
        super();
    }

    public MutablePoint(int x, int y) {
        super(x, y);
    }

    public MutablePoint(@NotNull Point point) {
        super(point);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(@NotNull Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    @NotNull
    public Point toImmutable() {
        return new Point(this);
    }
}
