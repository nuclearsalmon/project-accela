package net.accela.prisma.gui.drawable;

import net.accela.prisma.annotation.RectMutable;
import net.accela.prisma.event.ActivationEvent;
import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.DrawableIdentifier;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Point;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.geometry.Size;
import net.accela.prisma.gui.geometry.exception.RectOutOfBoundsException;
import net.accela.prisma.gui.text.BasicTextGrid;
import net.accela.prisma.gui.text.TextCharacter;
import net.accela.prisma.gui.text.TextGrid;
import net.accela.prisma.gui.text.effect.TextEffect;
import net.accela.prisma.input.lanterna.actions.KeyStroke;
import net.accela.server.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class InputField extends Drawable implements RectMutable {
    protected String text = "";
    protected int textMax = 0;
    protected boolean insert = false;

    protected TextEffect activeTextEffect = null;
    protected TextEffect inactiveTextEffect = null;

    protected BasicTextGrid canvas;
    protected Point point;

    int base = 0;
    int offset = 0;

    public InputField(@NotNull Rect rect) {
        this.canvas = new BasicTextGrid(rect.getSize());
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
        getTextGrid().resize(size, TextCharacter.DEFAULT);
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
            getTextGrid().resize(rect.getSize(), TextCharacter.DEFAULT);
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
    public @NotNull TextGrid getTextGrid() {
        return canvas;
    }

    /**
     * Applies changes
     */
    public void apply() {
        getTextGrid().setAllCharacters(
                TextCharacter.fromCharacter(' ', isActive ? activeTextEffect : inactiveTextEffect));

        // Populate the needed area of the cache with the text.
        // This will usually not fill the entire cache, but that is fine, as AccelaWM supports null points.
        for (int x = 0; x < getWidth(); x++) {
            // Submit to cache
            if (base + x < text.length()) {
                TextCharacter textCharacter = canvas.getCharacterAt(x, 0);
                canvas.setCharacterAt(x, 0, textCharacter.withCharacter(text.charAt(base + x)));
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

    public void write(char ch) {
        write(Character.toString(ch));
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
        System.out.println("deprecated" + start + ", new" + end);

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

    public void setActiveTextEffect(@Nullable TextEffect textEffect) {
        activeTextEffect = textEffect;
    }

    public void setInactiveTextEffect(@Nullable TextEffect textEffect) {
        inactiveTextEffect = textEffect;
    }

    //
    // Events
    //

    @EventHandler
    public void onKeyStroke(KeyStroke keyStroke) throws IOException {
        if (!isActive()) return;

        switch (keyStroke.getKeyType()) {
            case Character:
                Character character = keyStroke.getCharacter();
                if (character != null) {
                    write(character);
                    paint();
                }
                break;
            case Backspace:
                deleteLeft(1);
                apply();
                paint();
                break;
            case Delete:
                deleteRight(1);
                apply();
                paint();
                break;
            case ArrowLeft:
                scrollLeft(1);
                apply();
                paint();
                break;
            case ArrowRight:
                scrollRight(1);
                apply();
                paint();
                break;
            case Home:
                scrollToStart();
                apply();
                paint();
                break;
            case End:
                scrollToEnd();
                apply();
                paint();
                break;
            case Insert:
                insert = !insert;
                break;
        }
    }

    @Override
    public void onActivation(ActivationEvent event) throws NodeNotFoundException, IOException {
        super.onActivation(event);

        DrawableIdentifier identifier = event.getTarget();
        isActive = identifier == this.identifier;

        apply();
        paint();
    }
}
