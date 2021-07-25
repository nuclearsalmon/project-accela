package net.accela.prismatic.ui.drawable;

import net.accela.prismatic.CursorMode;
import net.accela.prismatic.Drawable;
import net.accela.prismatic.annotation.PointMutable;
import net.accela.prismatic.input.events.mouse.MouseInputEvent;
import net.accela.prismatic.input.lanterna.actions.InputEvent;
import net.accela.prismatic.ui.geometry.Point;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.text.BasicTextGrid;
import net.accela.prismatic.ui.text.TextCharacter;
import net.accela.prismatic.ui.text.TextGrid;
import net.accela.prismatic.ui.text.color.TextColor;
import net.accela.server.event.EventHandler;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class MouseCursor extends Drawable implements PointMutable {
    final TextGrid textGrid;

    public MouseCursor(@NotNull Plugin plugin) {
        super(new Rect(1, 1), plugin);
        textGrid = new BasicTextGrid(getSize());
    }

    //
    // Properties and flags
    //

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public @NotNull CursorMode getCursorMode() {
        return CursorMode.NONE;
    }

    @Override
    public boolean transparent() {
        return true;
    }

    //
    // Positioning
    //

    /**
     * @param point The relative position of this.
     */
    @Override
    public void setRelativePoint(@NotNull Point point) {
        internalSetPoint(point);
    }

    //
    // Painting
    //

    @Override
    @NotNull
    public TextGrid getTextGrid() {
        int r = ThreadLocalRandom.current().nextInt(256);
        int g = ThreadLocalRandom.current().nextInt(256);
        int b = ThreadLocalRandom.current().nextInt(256);

        textGrid.setCharacterAt(0, 0, TextCharacter.fromCharacter(':', TextColor.ANSI.DEFAULT, new TextColor.RGB(r, g, b)));

        return textGrid;
    }

    //
    // Event handling
    //

    @EventHandler
    void onMouseAction(@NotNull MouseInputEvent mouseInputEvent) {
        setRelativePoint(mouseInputEvent.getPosition());
    }

    @EventHandler
    void onKeyStroke(@NotNull InputEvent inputEvent) {
        switch (inputEvent.getKeyType()) {
            case MouseEvent:
                setRelativePoint(((MouseInputEvent) inputEvent).getPosition());
                break;
            case ArrowUp:
                setRelativePoint(getRelativePoint().withRelative(0, -1));
                break;
            case ArrowDown:
                setRelativePoint(getRelativePoint().withRelative(0, 1));
                break;
            case ArrowLeft:
                setRelativePoint(getRelativePoint().withRelative(-1, 0));
                break;
            case ArrowRight:
                setRelativePoint(getRelativePoint().withRelative(1, 0));
                break;
        }
    }
}
