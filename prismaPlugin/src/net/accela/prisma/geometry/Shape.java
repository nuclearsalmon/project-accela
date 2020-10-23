package net.accela.prisma.geometry;

public interface Shape {
    Rect getBounds();

    boolean contains(Point point);

    boolean contains(Rect rect);

    boolean intersects(Rect rect);
}
