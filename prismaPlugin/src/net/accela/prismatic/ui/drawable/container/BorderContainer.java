package net.accela.prismatic.ui.drawable.container;

import net.accela.prismatic.Drawable;
import net.accela.prismatic.ui.drawable.PrefilledBox;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.text.TextCharacter;
import net.accela.prismatic.ui.text.color.TextColor;
import net.accela.prismatic.util.chars.CP437Symbols;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BorderContainer extends BufferedContainer {
    Drawable topBorder;
    Drawable bottomBorder;
    Drawable leftBorder;
    Drawable rightBorder;

    public BorderContainer(@NotNull Rect rect, @NotNull Plugin plugin) {
        this(rect, plugin, null, null, null, null);
    }

    public BorderContainer(@NotNull Rect rect, @NotNull Plugin plugin,
                           @Nullable Drawable topBorder, @Nullable Drawable bottomBorder,
                           @Nullable Drawable leftBorder, @Nullable Drawable rightBorder) {
        super(rect, plugin);

        this.topBorder = topBorder;
        this.bottomBorder = bottomBorder;
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;

        if (topBorder == null) topBorder = new PrefilledBox(
                new Rect(0, 0, rect.getWidth(), 1), plugin, TextCharacter.fromCharacter(
                CP437Symbols.SINGLE_LINE_HORIZONTAL, TextColor.ANSI.GREEN, TextColor.ANSI.BLACK));
        if (bottomBorder == null) bottomBorder = new PrefilledBox(
                new Rect(0, rect.getHeight() - 1, rect.getWidth(), 1), plugin, TextCharacter.fromCharacter(
                CP437Symbols.SINGLE_LINE_HORIZONTAL, TextColor.ANSI.GREEN, TextColor.ANSI.BLACK));
        if (leftBorder == null) leftBorder = new PrefilledBox(
                new Rect(0, 1, 1, rect.getHeight() - 2), plugin, TextCharacter.fromCharacter(
                CP437Symbols.SINGLE_LINE_VERTICAL, TextColor.ANSI.GREEN, TextColor.ANSI.BLACK));
        if (rightBorder == null) rightBorder = new PrefilledBox(
                new Rect(rect.getWidth() - 1, 1, 1, rect.getHeight() - 2), plugin, TextCharacter.fromCharacter(
                CP437Symbols.SINGLE_LINE_VERTICAL, TextColor.ANSI.GREEN, TextColor.ANSI.BLACK));

        attach(topBorder);
        attach(bottomBorder);
        attach(leftBorder);
        attach(rightBorder);
    }
}
