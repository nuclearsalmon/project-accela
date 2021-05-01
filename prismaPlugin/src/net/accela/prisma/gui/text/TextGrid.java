package net.accela.prisma.gui.text;

import net.accela.prisma.annotation.Scrollable;
import net.accela.prisma.gui.geometry.Point;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.geometry.Size;
import org.jetbrains.annotations.NotNull;

public interface TextGrid extends Scrollable {
    boolean getTransparency();

    @NotNull
    Size getSize();

    TextCharacter getCharacterAt(@NotNull Point point);

    TextCharacter getCharacterAt(int column, int row);

    void setCharacterAt(@NotNull Point point, @NotNull TextCharacter character);

    void setCharacterAt(int column, int row, @NotNull TextCharacter character);

    void setAllCharacters(@NotNull TextCharacter character);

    /**
     * Resizes this {@link TextGrid}.
     *
     * @param newSize      The new {@link Size}.
     * @param newCharacter A {@link TextCharacter} to fill in empty areas caused by the resize.
     * @return An independent copy of this {@link TextGrid}, but at the {@link Size} specified.
     */
    @NotNull
    TextGrid resize(@NotNull Size newSize, @NotNull TextCharacter newCharacter);

    @NotNull
    TextGrid getCrop(@NotNull Rect cropRect);

    void copyTo(@NotNull TextGrid destination);

    default void copyTo(@NotNull TextGrid destination, @NotNull Rect rect) {
        copyTo(destination, rect.getMinY(), rect.getHeight(), rect.getMinX(), rect.getWidth(), 0, 0);
    }

    /**
     * Copies this TextImage's content to another TextImage. If the destination TextImage is larger than this
     * TextImage, the areas outside of the area that is written to will be untouched.
     *
     * @param destination             TextImage to copy to
     * @param startRowIndex           Which row in this image to copy from
     * @param rows                    How many rows to copy
     * @param startColumnIndex        Which column in this image to copy from
     * @param columns                 How many columns to copy
     * @param destinationRowOffset    Offset (in number of rows) in the target image where we want to first copied row to be
     * @param destinationColumnOffset Offset (in number of columns) in the target image where we want to first copied column to be
     */
    void copyTo(
            @NotNull TextGrid destination,
            int startRowIndex,
            int rows,
            int startColumnIndex,
            int columns,
            int destinationRowOffset,
            int destinationColumnOffset);

    @Override
    void scrollLines(int firstLine, int lastLine, int distance);
}
