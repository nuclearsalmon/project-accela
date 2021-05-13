package net.accela.prismatic.gui.drawable;

import net.accela.prismatic.annotation.RectMutable;
import net.accela.prismatic.event.ActivationEvent;
import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.DrawableIdentifier;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Point;
import net.accela.prismatic.gui.geometry.Rect;
import net.accela.prismatic.gui.geometry.Size;
import net.accela.prismatic.gui.geometry.exception.RectOutOfBoundsException;
import net.accela.prismatic.gui.text.BasicTextGrid;
import net.accela.prismatic.gui.text.TextCharacter;
import net.accela.prismatic.gui.text.TextGrid;
import net.accela.prismatic.gui.text.effect.TextEffect;
import net.accela.prismatic.input.lanterna.actions.InputEvent;
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

    int base = 0;
    int offset = 0;

    public InputField(@NotNull Rect rect) {
        super(rect);
        this.canvas = new BasicTextGrid(rect.getSize());
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
    public @NotNull Point getAbsoluteCursorRestingPoint() throws NodeNotFoundException {
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
        internalSetPoint(point);
    }

    @Override
    public void setRelativeRect(@NotNull Rect rect) {
        synchronized (this) {
            if (rect.getHeight() != 1) throw new RectOutOfBoundsException("Height must be 1");
            internalSetRect(rect);
        }
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
    public void onKeyStroke(InputEvent inputEvent) throws IOException {
        if (!isActive()) return;

        switch (inputEvent.getKeyType()) {
            case Character:
                Character character = inputEvent.getCharacter();
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

    @EventHandler
    public void onActivation(ActivationEvent event) throws NodeNotFoundException, IOException {
        DrawableIdentifier identifier = event.getTarget();
        isActive = identifier == this.identifier;

        apply();
        paint();
    }

    @Override
    protected void onResizeBeforePainting(@NotNull Size oldSize, @NotNull Size newSize) {
        getTextGrid().resize(newSize);
    }
}
