package net.accela.prisma.gui.text;

import net.accela.prisma.gui.geometry.Point;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.geometry.Size;
import net.accela.prisma.gui.text.color.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BasicTextGrid implements TextGrid {
    private final @NotNull Size size;
    private final @NotNull TextCharacter[][] buffer;
    private boolean transparency = false;

    /**
     * Creates a new BasicTextGrid with the specified size and fills it initially with space characters using the
     * default foreground and background color
     *
     * @param columns Size of the image in number of columns
     * @param rows    Size of the image in number of rows
     */
    public BasicTextGrid(int columns, int rows) {
        this(new Size(columns, rows));
    }

    /**
     * Creates a new BasicTextGrid with the specified size and fills it initially with space characters using the
     * default foreground and background color
     *
     * @param size Size to make the image
     */
    public BasicTextGrid(@NotNull Size size) {
        this(size, TextCharacter.fromCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT));
    }

    /**
     * Creates a new BasicTextGrid with a given size and a TextCharacter to initially fill it with
     *
     * @param size           Size of the image
     * @param initialContent What character to set as the initial content
     */
    public BasicTextGrid(@NotNull Size size, @NotNull TextCharacter initialContent) {
        this(size, new TextCharacter[0][], initialContent);
    }

    /**
     * Creates a new BasicTextGrid by copying a region of a two-dimensional array of TextCharacter:s. If the area to be
     * copied to larger than the source array, a filler character is used.
     *
     * @param size           Size to create the new BasicTextGrid as (and size to copy from the array)
     * @param toCopy         Array to copy initial data from
     * @param initialContent Filler character to use if the source array is smaller than the requested size
     */
    private BasicTextGrid(@NotNull Size size,
                          @NotNull TextCharacter[][] toCopy,
                          @NotNull TextCharacter initialContent) {
        this.size = size;

        int rows = size.getHeight();
        int columns = size.getWidth();
        buffer = new TextCharacter[rows][];
        for (int y = 0; y < rows; y++) {
            buffer[y] = new TextCharacter[columns];
            for (int x = 0; x < columns; x++) {
                if (y < toCopy.length && x < toCopy[y].length) {
                    buffer[y][x] = toCopy[y][x];
                } else {
                    buffer[y][x] = initialContent;
                }
            }
        }
    }

    public boolean getTransparency() {
        return transparency;
    }

    public void setTransparency(boolean transparency) {
        this.transparency = transparency;
    }

    @Override
    @NotNull
    public Size getSize() {
        return size;
    }

    @Override
    public void setAllCharacters(@NotNull TextCharacter character) {
        for (TextCharacter[] line : buffer) {
            Arrays.fill(line, character);
        }
    }

    @Override
    @NotNull
    public BasicTextGrid resize(@NotNull Size newSize, @NotNull TextCharacter filler) {
        if (newSize.getHeight() == buffer.length && newSize.getWidth() == buffer[0].length) {
            return this;
        }
        return new BasicTextGrid(newSize, buffer, filler);
    }

    @Override
    public @NotNull BasicTextGrid getCrop(@NotNull Rect cropRect) {
        BasicTextGrid croppedTextGrid = new BasicTextGrid(cropRect.getSize());
        copyTo(croppedTextGrid, cropRect);
        return croppedTextGrid;
    }

    @Override
    public void setCharacterAt(@NotNull Point position, @NotNull TextCharacter character) {
        setCharacterAt(position.getX(), position.getY(), character);
    }

    @Override
    public void setCharacterAt(int column, int row, @NotNull TextCharacter character) {
        if (column < 0 || row < 0 || row >= buffer.length || column >= buffer[0].length) {
            return;
        }

        // Double width character adjustments
        if (column > 0 && buffer[row][column - 1].isDoubleWidth()) {
            buffer[row][column - 1] = buffer[row][column - 1].withCharacter(' ');
        }

        // Assign the character at location we specified
        buffer[row][column] = character;

        // Double width character adjustments
        if (character.isDoubleWidth() && column + 1 < buffer[0].length) {
            buffer[row][column + 1] = character.withCharacter(' ');
        }
    }

    @Override
    public TextCharacter getCharacterAt(@NotNull Point position) {
        return getCharacterAt(position.getX(), position.getY());
    }

    @Override
    public TextCharacter getCharacterAt(int column, int row) {
        if (column < 0 || row < 0 || row >= buffer.length || column >= buffer[0].length) {
            return null;
        }

        return buffer[row][column];
    }

    @Override
    public void copyTo(@NotNull TextGrid source) {
        if (buffer.length > 0) {
            copyTo(source, 0, buffer.length, 0, buffer[0].length, 0, 0);
        }
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
     * @param destinationRowOffset    Offset (in number of rows) in the target image where we want the first copied row to be
     * @param destinationColumnOffset Offset (in number of columns) in the target image where we want the first copied column to be
     */
    @Override
    public void copyTo(
            @NotNull TextGrid destination,
            int startRowIndex,
            int rows,
            int startColumnIndex,
            int columns,
            int destinationRowOffset,
            int destinationColumnOffset) {

        // If the source image position is negative, offset the whole image
        if (startColumnIndex < 0) {
            destinationColumnOffset += -startColumnIndex;
            columns += startColumnIndex;
            startColumnIndex = 0;
        }
        if (startRowIndex < 0) {
            destinationRowOffset += -startRowIndex;
            rows += startRowIndex;
            startRowIndex = 0;
        }

        // If the destination offset is negative, adjust the source start indexes
        if (destinationColumnOffset < 0) {
            startColumnIndex -= destinationColumnOffset;
            columns += destinationColumnOffset;
            destinationColumnOffset = 0;
        }
        if (destinationRowOffset < 0) {
            startRowIndex -= destinationRowOffset;
            rows += destinationRowOffset;
            destinationRowOffset = 0;
        }

        //Make sure we can't copy more than is available
        rows = Math.min(buffer.length - startRowIndex, rows);
        columns = rows > 0 ? Math.min(buffer[0].length - startColumnIndex, columns) : 0;

        //Adjust target lengths as well
        columns = Math.min(destination.getSize().getWidth() - destinationColumnOffset, columns);
        rows = Math.min(destination.getSize().getHeight() - destinationRowOffset, rows);

        if (columns <= 0 || rows <= 0) {
            return;
        }

        Size destinationSize = destination.getSize();
        if (destination instanceof BasicTextGrid) {
            int targetRow = destinationRowOffset;
            for (int y = startRowIndex; y < startRowIndex + rows && targetRow < destinationSize.getHeight(); y++) {
                System.arraycopy(buffer[y], startColumnIndex, ((BasicTextGrid) destination).buffer[targetRow++], destinationColumnOffset, columns);
            }
        } else {
            //Manually copy character by character
            for (int y = startRowIndex; y < startRowIndex + rows; y++) {
                for (int x = startColumnIndex; x < startColumnIndex + columns; x++) {
                    TextCharacter character = buffer[y][x];
                    if (character.isDoubleWidth()) {
                        // If we're about to put a double-width character, first reset the character next to it
                        if (x + 1 < startColumnIndex + columns) {
                            destination.setCharacterAt(
                                    x - startColumnIndex + destinationColumnOffset,
                                    y - startRowIndex + destinationRowOffset,
                                    character.withCharacter(' '));
                        }
                        // If the last character is a double-width character, it would exceed the dimension so reset it
                        else if (x + 1 == startColumnIndex + columns) {
                            character = character.withCharacter(' ');
                        }
                    }
                    destination.setCharacterAt(
                            x - startColumnIndex + destinationColumnOffset,
                            y - startRowIndex + destinationRowOffset,
                            character);
                    if (character.isDoubleWidth()) {
                        x++;
                    }
                }
            }
        }

        // If the character immediately to the left in the destination is double-width, then reset it
        if (destinationColumnOffset > 0) {
            int destinationX = destinationColumnOffset - 1;
            for (int y = startRowIndex; y < startRowIndex + rows; y++) {
                int destinationY = y - startRowIndex + destinationRowOffset;
                TextCharacter neighbour = destination.getCharacterAt(destinationX, destinationY);
                if (neighbour.isDoubleWidth()) {
                    destination.setCharacterAt(destinationX, destinationY, neighbour.withCharacter(' '));
                }
            }
        }
    }

    private TextCharacter[] newBlankLine() {
        TextCharacter[] line = new TextCharacter[size.getWidth()];
        Arrays.fill(line, TextCharacter.DEFAULT);
        return line;
    }

    @Override
    public void scrollLines(int firstLine, int lastLine, int distance) {
        if (firstLine < 0) {
            firstLine = 0;
        }
        if (lastLine >= size.getHeight()) {
            lastLine = size.getHeight() - 1;
        }
        if (firstLine < lastLine) {
            if (distance > 0) {
                // scrolling up: start with first line as target:
                int curLine = firstLine;
                // copy lines from further "below":
                for (; curLine <= lastLine - distance; curLine++) {
                    buffer[curLine] = buffer[curLine + distance];
                }
                // blank out the remaining lines:
                for (; curLine <= lastLine; curLine++) {
                    buffer[curLine] = newBlankLine();
                }
            } else if (distance < 0) {
                // scrolling down: start with last line as target:
                int curLine = lastLine;
                distance = -distance;
                // copy lines from further "above":
                for (; curLine >= firstLine + distance; curLine--) {
                    buffer[curLine] = buffer[curLine - distance];
                }
                // blank out the remaining lines:
                for (; curLine >= firstLine; curLine--) {
                    buffer[curLine] = newBlankLine();
                }
            } /* else: distance == 0 => no-op */
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(size.getHeight() * (size.getWidth() + 1) + 50);
        sb.append('{').append(size.getWidth()).append('x').append(size.getHeight()).append('}').append('\n');
        for (TextCharacter[] line : buffer) {
            for (TextCharacter tc : line) {
                sb.append(tc.getCharacter());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
