package net.accela.prisma.util.ansi;

import net.accela.ansi.Patterns;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.session.TerminalReference;
import net.accela.prisma.util.ansi.compress.TerminalState;
import net.accela.prisma.util.canvas.Canvas;
import net.accela.prisma.util.canvas.Cell;
import net.accela.server.AccelaAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class AnsiFile {
    // Constants
    static final char SAUCE_CHAR = (char) 0x1A;

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

    enum ParserState {
        NORMAL,
        COMMENTS,
    }

    ParserState parserState = ParserState.NORMAL;

    // Temporary building list of comments
    final List<String> tmpComments = new ArrayList<>();

    // Character and SGR grids
    final Canvas canvas;

    // Sauce
    Sauce sauce = null;

    public AnsiFile(@NotNull File source, @NotNull TerminalReference terminal) throws IOException {
        // For saving cells. These will get placed onto a canvas later.
        final List<List<Cell>> cellRows = new ArrayList<>();

        // Trying with resources, this way the reader always gets closed at the end.
        try (BufferedReader reader = new BufferedReader((new FileReader(source)))) {
            // For keeping track of the virtual terminal state
            TerminalState terminalState = new TerminalState(terminal);

            while (true) {
                // Scan a line
                String line = reader.readLine();

                // No more to be read
                if (line == null) break;

                // Check the mode
                switch (parserState) {
                    case COMMENTS:
                        parseCOMNT(line);
                        continue; // Skip the rest of the loop
                    case NORMAL:
                        if (line.charAt(0) == SAUCE_CHAR) {
                            if (line.length() >= 5) {
                                switch (line.substring(1, 6)) {
                                    case "SAUCE": // Sauce
                                        parseSAUCE(line);
                                        break;
                                    case "COMNT": // Comment
                                        parserState = ParserState.COMMENTS;
                                        parseCOMNT(line);
                                        break;
                                    default: // Nonstandard
                                        AccelaAPI.getLogger().warning("Nonstandard SAUCE mode found in ANSI file");
                                        break;
                                }
                            } else {
                                AccelaAPI.getLogger().warning("Broken SAUCE found in ANSI file");
                            }
                            continue; // Skip the rest of this rotation in the loop
                        }
                        // Continue parsing
                        break;
                }

                // A list of cells in this row
                List<Cell> cellRow = new ArrayList<>();

                // ANSIMatch sequences in this line
                List<ANSIMatch> ANSIMatches = new ArrayList<>();
                Matcher ANSIMatcher = Patterns.ANSI8Bit.matcher(line);
                while (ANSIMatcher.find()) {
                    ANSIMatches.add(new ANSIMatch(ANSIMatcher.start(), ANSIMatcher.end(), ANSIMatcher.group()));
                }

                // Use the ANSIMatches to filter ANSI sequences and characters
                ANSIMatch nextANSIMatch = ANSIMatches.get(0);
                int currentMatchIndex = 0;
                int fileLineCursor = 0;
                while (fileLineCursor < line.length()) {
                    // If there's a match waiting, and the lineCursor has reached the start of it, do this
                    if (nextANSIMatch != null && fileLineCursor == nextANSIMatch.start) {
                        terminalState.apply(new SGRSequence(nextANSIMatch.group));

                        // Step forward the proper amount of characters
                        fileLineCursor += nextANSIMatch.length;

                        // Increase the index and grab the next match
                        currentMatchIndex++;
                        if (currentMatchIndex < ANSIMatches.size()) {
                            nextANSIMatch = ANSIMatches.get(currentMatchIndex);
                        } else {
                            nextANSIMatch = null;
                        }

                        // Don't execute the character part below,
                        // instead just continue to the next iteration of this loop
                        continue;
                    }

                    // Just characters left
                    cellRow.add(new Cell(
                            // FIXME: 12/19/20 use code points instead!
                            String.valueOf(line.charAt(fileLineCursor)),
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
            int lineWidth = row.size();
            if (lineWidth > maxWidth) maxWidth = lineWidth;
        }

        // Set a new canvas, as we now have the dimensions for it
        canvas = new Canvas(new Size(maxWidth, maxHeight));

        // Populate the canvas
        for (int y = 0; y < cellRows.size(); y++) {
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

    private void parseCOMNT(String line) {
        // Figure out where the metadata starts and ends
        int metaStart = getMetaStart(line);
        int metaEnd = 64 - metaStart;

        // Add comment
        tmpComments.add(line.substring(metaStart, metaEnd));

        // If there's more to the line
        if (line.length() - 1 > metaEnd) {
            metaStart = metaEnd + 1;
            String id = line.substring(metaStart, metaStart + 5);
            switch (id) {
                case "SAUCE":
                    parseSAUCE(line.substring(metaStart + 5));
                    break;
                case "COMNT":
                    parseCOMNT(line.substring(metaStart + 5));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown SAUCE ID: '" + id + "'");
            }
        }
    }

    private void parseSAUCE(String line) {
        // Figure out where the metadata starts and ends
        int metaStart = getMetaStart(line);

        sauce = new Sauce(line.substring(metaStart));
    }

    /**
     * Figure out the metadata starts
     *
     * @param line the line of metadata to analyse
     * @return The start of it
     */
    private int getMetaStart(String line) {
        if (line.length() > 5 && line.charAt(0) == (char) 0x1A) return 5; // +1 for EOF, +5 for id. (+0 +4 = 5)
        else return 0;
    }

    public static class Sauce {
        private String
                version = null,
                title = null,
                author = null,
                group = null,
                date = null,
                fileSize = null,
                dataType = null,
                fileType = null,
                TInfo1 = null,
                TInfo2 = null,
                TInfo3 = null,
                TInfo4 = null,
                commentsAmount = null,
                TFlags = null,
                TInfoS = null;

        Sauce(String line) {
            if (line.length() < 137) return;

            version = line.substring(6, 8);            // 2
            title = line.substring(9, 44);             // 35
            author = line.substring(45, 65);           // 20
            group = line.substring(66, 86);            // 20
            date = line.substring(87, 95);             // 8
            fileSize = line.substring(96, 99);         // 4
            dataType = line.substring(100, 100);       // 1
            fileType = line.substring(101, 101);       // 1
            TInfo1 = line.substring(102, 103);         // 2
            TInfo2 = line.substring(104, 115);         // 2
            TInfo3 = line.substring(116, 117);         // 2
            TInfo4 = line.substring(118, 119);         // 2
            commentsAmount = line.substring(120, 120); // 1
            TFlags = line.substring(121, 121);         // 1
            TInfoS = line.substring(122, 143);         // 22
        }

        public @Nullable String getVersion() {
            return version;
        }

        public @Nullable String getTitle() {
            return title;
        }

        public @Nullable String getAuthor() {
            return author;
        }

        public @Nullable String getGroup() {
            return group;
        }

        public @Nullable String getDate() {
            return date;
        }

        public @Nullable String getFileSize() {
            return fileSize;
        }

        public @Nullable String getDataType() {
            return dataType;
        }

        public @Nullable String getFileType() {
            return fileType;
        }

        public @Nullable String getTInfo1() {
            return TInfo1;
        }

        public @Nullable String getTInfo2() {
            return TInfo2;
        }

        public @Nullable String getTInfo3() {
            return TInfo3;
        }

        public @Nullable String getTInfo4() {
            return TInfo4;
        }

        public @Nullable String getCommentsAmount() {
            return commentsAmount;
        }

        public @Nullable String getTFlags() {
            return TFlags;
        }

        public @Nullable String getTInfoS() {
            return TInfoS;
        }
    }

    public @NotNull Canvas getCanvas() {
        return canvas;
    }

    public Sauce getSauce() {
        return sauce;
    }
}
