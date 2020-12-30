package net.accela.prisma.drawable;

import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableIdentifier;
import net.accela.prisma.drawable.property.RectMutable;
import net.accela.prisma.event.ActivationEvent;
import net.accela.prisma.event.SpecialInputEvent;
import net.accela.prisma.event.StringInputEvent;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import net.accela.prisma.util.canvas.Canvas;
import net.accela.prisma.util.canvas.Cell;
import net.accela.server.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputField extends Drawable implements RectMutable {
    protected String text = "";
    protected int textMax = 0;
    protected boolean insert = false;

    protected SGRSequence activeSGR = null;
    protected SGRSequence inactiveSGR = null;

    protected Canvas canvas;
    protected Point point;

    int base = 0;
    int offset = 0;

    public InputField(@NotNull Rect rect) {
        this.canvas = new Canvas(rect.getSize());
        this.point = rect.getStartPoint();
    }

    //
    // Properties
    //

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public @NotNull CursorMode getCursorMode() {
        return CursorMode.TERMINAL_RENDERED;
    }


    @Override
    public boolean cursorEnabled() {
        return true;
    }

    @Override
    public @NotNull Point getCursorRestingPoint() throws NodeNotFoundException {
        // The starting point
        Point startingPoint = getAbsoluteRect().getStartPoint();

        // A new point with an offset applied
        return new Point(startingPoint.getX() + offset, startingPoint.getY());
    }

    @Override
    public boolean transparent() {
        return false;
    }

    public String getText() {
        return text;
    }

    //
    // Positioning
    //

    @Override
    public void setSize(@NotNull Size size) throws RectOutOfBoundsException {
        if (size.getHeight() != 1) throw new RectOutOfBoundsException("Height must be 1");
        getCanvas().setSize(size);
    }

    /**
     * @param point The relative position of this.
     */
    @Override
    public void setRelativePoint(@NotNull Point point) throws NodeNotFoundException {
        this.point = point;
    }

    @Override
    public void setRelativeRect(@NotNull Rect rect) {
        synchronized (this) {
            if (rect.getHeight() != 1) throw new RectOutOfBoundsException("Height must be 1");
            point = rect.getStartPoint();
            getCanvas().setSize(rect.getSize());
        }
    }

    /**
     * @return The size and relative position of this {@link Drawable}.
     */
    @Override
    public @NotNull Rect getRelativeRect() {
        return new Rect(point, canvas.getSize());
    }

    /**
     * @return This {@link Drawable}'s relative position.
     */
    @Override
    public @NotNull Point getRelativePoint() {
        return point;
    }

    //
    // Painting and controls
    //

    @Override
    @NotNull
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Applies changes
     */
    public void apply() {
        getCanvas().fill(getZeroRect(), " ", isActive ? activeSGR : inactiveSGR);

        // Populate the needed area of the cache with the text.
        // This will usually not fill the entire cache, but that is fine, as AccelaWM supports null points.
        for (int x = 0; x < getWidth(); x++) {
            // Submit to cache
            if (base + x < text.length()) {
                Cell cell = canvas.get(x, 0);
                canvas.set(x, 0,
                        new Cell(Character.toString(text.charAt(base + x)),
                                cell == null ? null : cell.getSequence())
                );
            } else break;
        }
    }

    public void scrollToStart() {
        offset = 0;
        base = 0;
    }

    public void scrollToEnd() {
        scrollRight(text.length());
    }

    public void scrollLeft(int amount) {
        for (int i = 0; i < amount; i++) {
            if (offset + base > 0) {
                if (offset > 0) offset--;
                else {
                    base--;
                }
            } else break;
        }
    }

    public void scrollRight(int amount) {
        for (int i = 0; i < amount; i++) {
            if (offset + base < text.length()) {
                if (offset < getWidth()) offset++;
                else {
                    base++;
                }
            } else break;
        }
    }

    public void setString(String string) {
        if (textMax > 0 && string.length() > textMax) {
            string = string.substring(0, textMax);
        }
        text = string;

        scrollToEnd();
    }

    public void write(String insertString) {
        if (textMax < 1 | (textMax > 0 && textMax >= text.length() + insertString.length())) {
            int cursor = base + offset;
            String tmpContents = text.substring(0, cursor) + insertString;

            if (insert && cursor + 1 <= text.length()) tmpContents += text.substring(cursor + 1);
            else tmpContents += text.substring(cursor);

            text = tmpContents;

            // Change cursorX accordingly
            scrollRight(insertString.length());
        }
    }

    public void deleteRight(int amount) {
        int start = base + offset;
        int end = base + offset + amount;
        System.out.println("old" + start + ", new" + end);

        if (end >= text.length()) end = text.length();

        text = new StringBuilder(text).delete(start, end).toString();
    }

    public void deleteLeft(int amount) {
        int oldCursor = base + offset;
        int newCursor = oldCursor - amount;

        if (newCursor < 0) {
            newCursor = 0;
        }

        StringBuilder builder = new StringBuilder(text);
        builder.delete(newCursor, oldCursor);
        text = builder.toString();
        scrollLeft(amount);

    }

    public void setActiveSGR(@Nullable SGRSequence sequence) {
        activeSGR = sequence;
    }

    public void setInactiveSGR(@Nullable SGRSequence sequence) {
        inactiveSGR = sequence;
    }

    //
    // Events
    //

    @EventHandler
    public void onStringInput(StringInputEvent event) throws NodeNotFoundException {
        if (!isActive()) return;

        write(event.getEntry());
        apply();
        paint();
    }

    @EventHandler
    public void onSpecialInput(SpecialInputEvent event) throws NodeNotFoundException {
        if (!isActive()) return;

        switch (event.getKey()) {
            case BS:
            case DEL:
                deleteLeft(1);
                apply();
                paint();
                break;
            case DELETE:
                deleteRight(1);
                apply();
                paint();
                break;
            case LEFT:
                scrollLeft(1);
                apply();
                paint();
                break;
            case RIGHT:
                scrollRight(1);
                apply();
                paint();
                break;
            case HOME:
                scrollToStart();
                apply();
                paint();
                break;
            case END:
                scrollToEnd();
                apply();
                paint();
                break;
            case INSERT:
                insert = !insert;
                break;
        }
    }

    @Override
    public void onActivation(ActivationEvent event) throws NodeNotFoundException {
        super.onActivation(event);

        DrawableIdentifier identifier = event.getTarget();
        isActive = identifier == this.identifier;

        apply();
        paint();
    }
}
