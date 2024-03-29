package net.accela.prismatic.file.ansi;

import net.accela.prismatic.sequence.SGRAttribute;
import net.accela.prismatic.sequence.SGRStatement;
import net.accela.prismatic.terminal.Terminal;
import net.accela.prismatic.ui.geometry.Size;
import net.accela.prismatic.ui.text.BasicTextGrid;
import net.accela.prismatic.ui.text.TextCharacter;
import net.accela.prismatic.ui.text.effect.StyleSet;
import net.accela.prismatic.util.ANSIPatterns;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
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
    /** The size of the Sauce block, excluding comments */
    static final int SAUCE_BLOCK_SIZE = 128;

    private static class MatchInfo {
        public final int start;
        @SuppressWarnings("unused")
        public final int end;
        public final String group;
        public final int length;

        MatchInfo(int start, int end, String group) {
            this.start = start;
            this.end = end;
            this.group = group;
            this.length = end - start;
        }
    }

    /**
     * BasicTextGrid representation
     */
    final BasicTextGrid canvas;

    /**
     * Sauce
     */
    public final Sauce sauce;

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
    public static @Nullable Sauce readSauce(final @NotNull File source) throws IOException {
        return readSauce(source, Terminal.IBM437_CHARSET);
    }

    /**
     * Reads a sauce record and any comments data if available.
     *
     * @param source  The file to read from.
     * @param charset The charset to use. IBM_437 is recommended unless explicitly specified.
     * @return A {@link Sauce} instance, or null if no sauce records was found.
     * @throws IOException           If exception occurs when reading file.
     * @throws InvalidSauceException If the sauce is invalid or malformed.
     */
    public static @Nullable Sauce readSauce(final @NotNull File source, final @NotNull Charset charset) throws IOException, InvalidSauceException {
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
            String id = unsafeGetString(sauceBuffer, 0, 5, charset);
            if (!id.equals("SAUCE")) throw new InvalidSauceException(String.format("Bad SAUCE ID '%s'", id));

            // Read other character data
            String version = unsafeGetString(sauceBuffer, 5, 7, charset);
            String title = unsafeGetString(sauceBuffer, 7, 42, charset);
            String author = unsafeGetString(sauceBuffer, 42, 62, charset);
            String group = unsafeGetString(sauceBuffer, 62, 82, charset);
            String date = unsafeGetString(sauceBuffer, 82, 90, charset);
            //int fileSize = unsafeGetByteBuffer(buffer, 90, 94).getInt();
            int dataType = unsafeGetInt(sauceBuffer, 94, 95);
            int fileType = unsafeGetInt(sauceBuffer, 95, 96);
            int typeInfo1 = unsafeGetInt(sauceBuffer, 96, 98);
            int typeInfo2 = unsafeGetInt(sauceBuffer, 100, 102);
            int typeInfo3 = unsafeGetInt(sauceBuffer, 102, 104);
            int typeInfo4 = unsafeGetInt(sauceBuffer, 104, 106);
            int commentsAmount = unsafeGetInt(sauceBuffer, 106, 107);
            String comments = null;
            int typeFlags = (byte) unsafeGetInt(sauceBuffer, 107, 108);
            String typeInfoString = unsafeGetString(sauceBuffer, 108, 130, charset);

            // Calculate and read any comments
            if (commentsAmount > 0) {
                final int commentsStartPos = (int) (realFileSize - (64 * commentsAmount) - SAUCE_BLOCK_SIZE);
                final int commentsEndPos = (int) (realFileSize - SAUCE_BLOCK_SIZE);
                byte[] commentBuffer = new byte[commentsEndPos - commentsStartPos];
                fileInputStream.getChannel().position(0);
                //noinspection ResultOfMethodCallIgnored
                fileInputStream.read(commentBuffer, 0, commentBuffer.length);

                comments = unsafeGetString(commentBuffer, 0, commentBuffer.length, charset);
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
    private static @NotNull String unsafeGetString(byte[] buffer, int startIncl, int endExcl,
                                                   final @NotNull Charset charset) {
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
        return new String(stringBytes, charset).stripTrailing();
    }

    /**
     * No validation.
     */
    private static int unsafeGetInt(byte[] buffer, int startIncl, int endExcl) {
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

    public AnsiFile(final @NotNull File source, final @NotNull Charset charset, final boolean boldAsBright) throws IOException {
        this.source = source;

        // Read sauce and comments data
        this.sauce = readSauce(source);

        // For saving textCharacters. These will get placed onto a canvas later.
        final List<List<TextCharacter>> textCharacterRows = new ArrayList<>();

        // Trying with resources, this way the reader always gets closed at the end.
        try (BufferedReader reader = new BufferedReader((new FileReader(source, charset)))) {
            // For keeping track of the various SGRStatements
            StyleSet.Set styleSet = new StyleSet.Set();

            while (true) {
                // Scan a line
                String line = reader.readLine();

                // No more to be read
                if (line == null || (line.length() > 0 && line.charAt(0) == SAUCE_INIT_CHAR)) break;
                    // Empty line
                else if (line.length() == 0) continue;

                // A list of textCharacters in this row
                List<TextCharacter> textCharacterRow = new ArrayList<>();

                // MatchInfo sequences in this line
                List<MatchInfo> MatchInfo = new ArrayList<>();
                Matcher ANSIMatcher = ANSIPatterns.ANSI8Bit.matcher(line);
                while (ANSIMatcher.find()) {
                    MatchInfo.add(new MatchInfo(ANSIMatcher.start(), ANSIMatcher.end(), ANSIMatcher.group()));
                }

                // Use the MatchInfo to distinguish between ANSI sequences and regular characters and separate them
                MatchInfo nextMatchInfo = null;
                if (MatchInfo.size() > 0) nextMatchInfo = MatchInfo.remove(0);

                boolean brightModifier = false;  // For filtering bold colors
                int fileLineCursor = 0;  // The cursor for the current line
                while (fileLineCursor < line.length()) {
                    // If there's a match waiting, and the lineCursor has reached the start of it, do this
                    if (nextMatchInfo != null && fileLineCursor == nextMatchInfo.start) {
                        // Convert to statements
                        final SGRStatement[] statements = SGRStatement.fromSGRString(nextMatchInfo.group);

                        // Filter bold if asked to
                        if (boldAsBright) {
                            for (SGRStatement statement : statements) {
                                final SGRAttribute attribute = statement.getAttribute();
                                switch (attribute) {
                                    case INTENSITY_BRIGHT_OR_BOLD -> brightModifier = true;
                                    case RESET, INTENSITY_OFF, INTENSITY_DIM_OR_THIN -> brightModifier = false;
                                    default -> {
                                        // Only do the below code if the attribute is an ANSI color
                                        if (!attribute.isANSITextColor()) break;

                                        // Extract codes
                                        int colorPrefix = attribute.getCode() / 10;
                                        int colorIndex = attribute.getCode() % 10;

                                        // If it's the default color
                                        if (colorIndex == 9) break;

                                        // If it's not already bright and the bright modifier is enabled, modify.
                                        boolean isBackgroundColor = colorPrefix == 4 || colorPrefix == 10;
                                        boolean isBrightByDefault = colorPrefix == 9 || colorPrefix == 10;
                                        if (!isBackgroundColor && !isBrightByDefault && brightModifier) {
                                            // Create a modified statement
                                            int newAttributeIndex = (colorPrefix + 6) * 10 + colorIndex;
                                            SGRAttribute newAttribute = SGRAttribute.fromInt(newAttributeIndex);
                                            SGRStatement[] newStatements = SGRStatement.fromSGRAttribute(newAttribute);

                                            // Verify
                                            if (newStatements.length > 1)
                                                throw new IllegalStateException("Returned more SGRStatements than expected.");

                                            // Replace with modified statement
                                            statement = newStatements[0];
                                        }
                                    }
                                }

                                // Consume statement
                                styleSet.consume(statement);
                            }
                        } else {
                            // Consume statements as-is
                            styleSet.consume(statements);
                        }

                        // Step forward the proper amount of characters
                        fileLineCursor += nextMatchInfo.length;

                        // Grab the next match, if any
                        if (MatchInfo.size() > 0) {
                            nextMatchInfo = MatchInfo.remove(0);
                        } else {
                            nextMatchInfo = null;
                        }
                    }
                    // Just characters left
                    else {
                        // Parse character
                        char chr = line.charAt(fileLineCursor);

                        // Verify that we did not reach the SAUCE record. If we did, then break.
                        if (chr == SAUCE_INIT_CHAR) break;

                        // Create TextCharacter and add to the row
                        textCharacterRow.addAll(Arrays.asList(TextCharacter.fromString(
                                Character.toString(line.codePointAt(fileLineCursor)),
                                styleSet.getForegroundColor(),
                                styleSet.getBackgroundColor(),
                                styleSet.getActiveModifiers()
                        )));

                        // Step forward one character
                        fileLineCursor++;
                    }
                }

                // Add to the list of rows;
                textCharacterRows.add(textCharacterRow);
            }
        }

        // Figure out the canvas dimensions
        int maxWidth = 0, maxHeight = textCharacterRows.size();
        for (List<TextCharacter> row : textCharacterRows) {
            // Update width if needed
            int rowWidth = row.size();
            if (rowWidth > maxWidth) maxWidth = rowWidth;
        }
        Size size = new Size(maxWidth, maxHeight);

        // Set a new canvas, as we now have the dimensions for it
        canvas = new BasicTextGrid(size);

        // Populate the canvas
        for (int y = 0; y < maxHeight; y++) {
            List<TextCharacter> row = textCharacterRows.get(y);
            int width = row.size();

            for (int x = 0; x < width; x++) {
                // Get textCharacter
                TextCharacter textCharacter = row.get(x);

                // Set TextCharacter to BasicTextGrid
                canvas.setCharacterAt(x, y, textCharacter);
            }
        }
    }

    public @NotNull BasicTextGrid getTextGrid() {
        return canvas;
    }

    public Sauce getSauce() {
        return sauce;
    }
}
