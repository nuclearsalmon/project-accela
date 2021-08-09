package net.accela.prismatic.ui.drawable;

import net.accela.prismatic.Drawable;
import net.accela.prismatic.behaviour.CursorMode;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.text.BasicTextGrid;
import net.accela.prismatic.ui.text.TextCharacter;
import net.accela.prismatic.ui.text.TextGrid;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PrefilledBox extends Drawable {
    final @NotNull TextGrid textGrid;
    final @NotNull TextCharacter textCharacter;

    public PrefilledBox(@NotNull Rect rect, @NotNull Plugin plugin, @NotNull TextCharacter textCharacter) {
        super(rect, plugin);
        this.textCharacter = textCharacter;
        this.textGrid = new BasicTextGrid(rect.getSize());
        generateInitialContents();
    }

    //
    // Properties
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
        return false;
    }

    public boolean doAutoResize() {
        return false;
    }

    //
    // Painting
    //

    @Override
    public @NotNull TextGrid getTextGrid() {
        return textGrid;
    }

    protected void generateInitialContents() {
        textGrid.setAllCharacters(textCharacter);
    }
}
