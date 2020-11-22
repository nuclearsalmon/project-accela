package net.accela.prisma.drawables;

import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableIdentifier;
import net.accela.prisma.event.ActivationEvent;
import net.accela.prisma.event.SpecialInputEvent;
import net.accela.prisma.event.StringInputEvent;
import net.accela.prisma.exception.DeadWMException;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.util.Canvas;
import net.accela.server.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputField extends Drawable {
    protected String textContent = "";
    protected int textMax = 0;
    protected boolean insert = false;

    protected SGRSequence activeSGR = null;
    protected SGRSequence inactiveSGR = null;

    protected Canvas canvas = new Canvas(getSize());

    int base = 0;
    int offset = 0;

    public InputField(@NotNull Rect rect) {
        super(rect);
    }

    @Override
    public void setRect(@NotNull Rect newRect) throws NodeNotFoundException {
        synchronized (this) {
            getCanvas().setSize(newRect.getSize());
            super.setRect(newRect);
        }
    }

    public String getContent() {
        return textContent;
    }

    @Override
    protected @NotNull Canvas getCanvas() {
        return canvas;
    }

    /**
     * Applies changes
     */
    public void apply() throws DeadWMException {
        getCanvas().fill(getZeroRect(), " ", isActive ? activeSGR : inactiveSGR);

        // Populate the needed area of the cache with the text.
        // This will usually not fill the entire cache, but that is fine, as AccelaWM supports null points.
        for (int x = 0; x < getWidth(); x++) {
            // Submit to cache
            if (base + x < textContent.length()) {
                Canvas.Cell cell = canvas.get(x, 0);
                canvas.set(x, 0,
                        new Canvas.Cell(Character.toString(textContent.charAt(base + x)),
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
        scrollRight(textContent.length());
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
            if (offset + base < textContent.length()) {
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
        textContent = string;

        scrollToEnd();
    }

    public void write(String insertString) {
        if (textMax < 1 | (textMax > 0 && textMax >= textContent.length() + insertString.length())) {
            int cursor = base + offset;
            String tmpContents = textContent.substring(0, cursor) + insertString;

            if (insert && cursor + 1 <= textContent.length()) tmpContents += textContent.substring(cursor + 1);
            else tmpContents += textContent.substring(cursor);

            textContent = tmpContents;

            // Change cursorX accordingly
            scrollRight(insertString.length());
        }
    }

    public void deleteRight(int amount) {
        int start = base + offset;
        int end = base + offset + amount;
        System.out.println("old" + start + ", new" + end);

        if (end >= textContent.length()) end = textContent.length();

        textContent = new StringBuilder(textContent).delete(start, end).toString();
    }

    public void deleteLeft(int amount) {
        int oldCursor = base + offset;
        int newCursor = oldCursor - amount;

        if (newCursor < 0) {
            newCursor = 0;
        }

        StringBuilder builder = new StringBuilder(textContent);
        builder.delete(newCursor, oldCursor);
        textContent = builder.toString();
        scrollLeft(amount);

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

    public void setActiveSGR(@Nullable SGRSequence sequence) {
        activeSGR = sequence;
    }

    public void setInactiveSGR(@Nullable SGRSequence sequence) {
        inactiveSGR = sequence;
    }

    /*
    @Override
    public void trySetRect(@NotNull Rect relativeRect) throws RectOutOfBoundsException {
        if(relativeRect.getHeight() != 1) throw new RectOutOfBoundsException("Height must be 1");
        super.trySetRect(relativeRect);
    }

    @Override
    public void trySetSize(@NotNull Size size) throws RectOutOfBoundsException {
        if(size.getHeight() != 1) throw new RectOutOfBoundsException("Height must be 1");
        super.trySetSize(size);
    }
     */

    ///
    /// EVENTS
    ///

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
        boolean isActiveOld = isActive;
        isActive = identifier == this.identifier;

        apply();
        paint();
    }
}
