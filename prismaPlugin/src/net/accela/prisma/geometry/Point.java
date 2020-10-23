package net.accela.prisma.geometry;

import org.jetbrains.annotations.NotNull;

public class Point {
    int x, y;

    public Point() {
        this.x = 0;
        this.y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(@NotNull Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double distanceSq(@NotNull Point pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        return px * px + py * py;
    }

    public double distance(@NotNull Point pt) {
        int px = pt.getX() - this.getX();
        int py = pt.getY() - this.getY();
        return Math.sqrt(px * px + py * py);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point point = (Point) obj;
            return point.x == this.x && point.y == this.y;
        }
        return super.equals(obj);
    }

    @NotNull
    @Override
    public String toString() {
        return this.getClass().getName() + "[x=" + this.x + ",y=" + this.y + "]";
    }

    @NotNull
    public MutablePoint toMutable() {
        return new MutablePoint(this);
    }
}
