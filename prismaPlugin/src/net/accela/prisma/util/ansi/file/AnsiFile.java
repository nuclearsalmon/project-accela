package net.accela.prisma.util.ansi.file;

import net.accela.ansi.Patterns;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.session.Terminal;
import net.accela.prisma.session.TerminalReference;
import net.accela.prisma.util.ansi.compress.TerminalState;
import net.accela.prisma.util.canvas.Canvas;
import net.accela.prisma.util.canvas.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class AnsiFile {
    // Constants
    /**
     * The EOF character used to mark the beginnings of a sauce record
     */
    static final char SAUCE_INIT_CHAR = (char) 0x1A;
    /**
     * The size of the Sauce block, excluding comments
     */
    static final int SAUCE_BLOCK_SIZE = 128;

    private static class ANSIMatch {
        public final int start;
        public final int end;
        public final String group;
        public final int length;

        ANSIMatch(int start, int end, String group) {
            this.start = start;
            this.end = end;
            this.group = group;
            this.length = end - start;
        }
    }

    /**
     * Canvas representation
     */
    final Canvas canvas;

    /**
     * Sauce
     */
    final Sauce sauce;

    /**
     * Source file
     */
    final File source;

    /**
     * Reads a sauce record and any comments data if available.
     *
     * @param source The file to read from.
     * @return A {@link Sauce} instance, or null if no sauce records was found.
     * @throws IOException           If exception occurs when reading file.
     * @throws InvalidSauceException If the sauce is invalid or malformed.
     */
    public static @Nullable Sauce readSauce(@NotNull File source) throws IOException, InvalidSauceException {
        long realFileSize = source.length();

        // No sauce
        if (realFileSize < SAUCE_BLOCK_SIZE) return null;

        try (FileInputStream fileInputStream = new FileInputStream(source)) {
            byte[] sauceBuffer = new byte[SAUCE_BLOCK_SIZE];
            fileInputStream.getChannel().position(realFileSize - SAUCE_BLOCK_SIZE);
            int read = fileInputStream.read(sauceBuffer, 0, SAUCE_BLOCK_SIZE);

            // Validate that the amount of bytes read matches the sauce block size
            if (read < SAUCE_BLOCK_SIZE) throw new InvalidSauceException("Bad sauce block length");

            // Read and validate ID
            String id = trustedGetString(sauceBuffer, 0, 5);
            if (!id.equals("SAUCE")) throw new InvalidSauceException(String.format("Bad SAUCE ID '%s'", id));

            // Read other character data
            String version = trustedGetString(sauceBuffer, 5, 7);
            String title = trustedGetString(sauceBuffer, 7, 42);
            String author = trustedGetString(sauceBuffer, 42, 62);
            String group = trustedGetString(sauceBuffer, 62, 82);
            String date = trustedGetString(sauceBuffer, 82, 90);
            //int fileSize = trustedGetByteBuffer(buffer, 90, 94).getInt();
            int dataType = trustedGetInt(sauceBuffer, 94, 95);
            int fileType = trustedGetInt(sauceBuffer, 95, 96);
            int typeInfo1 = trustedGetInt(sauceBuffer, 96, 98);
            int typeInfo2 = trustedGetInt(sauceBuffer, 100, 102);
            int typeInfo3 = trustedGetInt(sauceBuffer, 102, 104);
            int typeInfo4 = trustedGetInt(sauceBuffer, 104, 106);
            int commentsAmount = trustedGetInt(sauceBuffer, 106, 107);
            String comments = null;
            int typeFlags = (byte) trustedGetInt(sauceBuffer, 107, 108);
            String typeInfoString = trustedGetString(sauceBuffer, 108, 130);

            // Calculate and read any comments
            if (commentsAmount > 0) {
                final int commentsStartPos = (int) (realFileSize - (64 * commentsAmount) - SAUCE_BLOCK_SIZE);
                final int commentsEndPos = (int) (realFileSize - SAUCE_BLOCK_SIZE);
                byte[] commentBuffer = new byte[commentsEndPos - commentsStartPos];
                fileInputStream.getChannel().position(0);
                fileInputStream.read(commentBuffer, 0, commentBuffer.length);

                comments = trustedGetString(commentBuffer, 0, commentBuffer.length);
            }

            // Create a new Sauce data object from the gathered parameters
            return new Sauce(version, title, author, group, date,
                    dataType, fileType, typeInfo1, typeInfo2, typeInfo3, typeInfo4,
                    typeFlags, typeInfoString, comments);
        }
    }

    /**
     * No validation.
     */
    private static @NotNull String trustedGetString(byte[] buffer, int startIncl, int endExcl) {
        byte[] stringBytes = Arrays.copyOfRange(buffer, startIncl, endExcl);

        // Precautionary measure taken to both handle ZStrings as well as poorly padded String(s).
        for (int i = 0; i < stringBytes.length; i++) {
            byte by = stringBytes[i];
            if (by == 0x0) {
                stringBytes = Arrays.copyOfRange(stringBytes, 0, i);
                break;
            }
        }

        // Convert to a String using the correct charset and remove trailing spaces if needed
        return new String(stringBytes, Terminal.IBM437_CHARSET).stripTrailing();
    }

    /**
     * No validation.
     */
    private static int trustedGetInt(byte[] buffer, int startIncl, int endExcl) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byte[] bytes = Arrays.copyOfRange(buffer, startIncl, endExcl);
        for (byte by : bytes) {
            byteBuffer.put(by);
        }
        return byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getShort();
        /*
        return new BigInteger(1, Arrays.copyOfRange(buffer, startIncl, endExcl)).intValue();
         */
    }

    public AnsiFile(final @NotNull File source, final @NotNull TerminalReference terminal) throws IOException {
        this.source = source;

        // Read sauce and comments data
        this.sauce = readSauce(source);

        // For saving cells. These will get placed onto a canvas later.
        final List<List<Cell>> cellRows = new ArrayList<>();

        // Trying with resources, this way the reader always gets closed at the end.
        try (BufferedReader reader = new BufferedReader((new FileReader(source, Terminal.IBM437_CHARSET)))) {
            // For keeping track of the virtual terminal state
            TerminalState terminalState = new TerminalState(terminal);

            while (true) {
                // Scan a line
                String line = reader.readLine();

                // No more to be read
                if (line == null) break;
                    // Empty line
                else if (line.length() <= 0) continue;

                //fixme remove
                System.out.println(line);

                // A list of cells in this row
                List<Cell> cellRow = new ArrayList<>();

                // ANSIMatch sequences in this line
                List<ANSIMatch> ANSIMatches = new ArrayList<>();
                Matcher ANSIMatcher = Patterns.ANSI8Bit.matcher(line);
                while (ANSIMatcher.find()) {
                    ANSIMatches.add(new ANSIMatch(ANSIMatcher.start(), ANSIMatcher.end(), ANSIMatcher.group()));
                }

                // Use the ANSIMatches to distinguish between ANSI sequences and regular characters and separate them
                ANSIMatch nextANSIMatch = null;
                if (ANSIMatches.size() > 0) nextANSIMatch = ANSIMatches.remove(0);

                int fileLineCursor = 0;
                while (fileLineCursor < line.length()) {
                    // If there's a match waiting, and the lineCursor has reached the start of it, do this
                    if (nextANSIMatch != null && fileLineCursor == nextANSIMatch.start) {
                        terminalState.apply(new SGRSequence(nextANSIMatch.group));

                        // Step forward the proper amount of characters
                        fileLineCursor += nextANSIMatch.length;

                        // Grab the next match, if any
                        if (ANSIMatches.size() > 0) {
                            nextANSIMatch = ANSIMatches.remove(0);
                        } else {
                            nextANSIMatch = null;
                        }

                        // Don't execute the character part below,
                        // instead just continue to the next iteration of this loop
                        continue;
                    }

                    // Just characters left
                    char chr = line.charAt(fileLineCursor);
                    if (chr == SAUCE_INIT_CHAR) break;

                    cellRow.add(new Cell(
                            Character.toString(line.codePointAt(fileLineCursor)),
                            new SGRSequence(terminalState.getStatements())
                    ));

                    // Step forward one character
                    fileLineCursor++;
                }

                // Add to the list of rows;
                cellRows.add(cellRow);
            }
        }

        // Figure out the canvas dimensions
        int maxWidth = 0, maxHeight = cellRows.size();
        for (List<Cell> row : cellRows) {
            // Update width if needed
            int rowWidth = row.size();
            if (rowWidth > maxWidth) maxWidth = rowWidth;
        }
        Size size = new Size(maxWidth, maxHeight);

        // Set a new canvas, as we now have the dimensions for it
        canvas = new Canvas(size);

        // Populate the canvas
        for (int y = 0; y < maxHeight; y++) {
            List<Cell> row = cellRows.get(y);
            int width = row.size();

            for (int x = 0; x < width; x++) {
                // Get cell
                Cell cell = row.get(x);

                // Set Cell to Canvas
                canvas.set(x, y, cell);
            }
        }
    }

    public @NotNull Canvas getCanvas() {
        return canvas;
    }

    public Sauce getSauce() {
        return sauce;
    }
}
