package net.accela.prisma.drawables;

import net.accela.ansi.Patterns;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.util.MutableGrid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public class AnsiFile {
    // Constants
    static final char SAUCE_CHAR = (char) 0x1A;

    enum ParserState {
        CHARS,
        COMMENTS,
    }

    static class ANSIMatch {
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

    // Parser related
    ParserState parserState = ParserState.CHARS;
    List<String> tmpComments = new ArrayList<>();

    // Character and SGR grids
    MutableGrid<Character> charGrid;
    MutableGrid<SGRSequence> SGRGrid;

    // Sauce
    Sauce sauce;

    public AnsiFile(File source) throws IOException {
        final BufferedReader reader = new BufferedReader((new FileReader(source)));

        // All lines, separated
        List<List<Character>> charLines = new ArrayList<>();
        List<List<SGRSequence>> SGRLines = new ArrayList<>();

        // gridWidth is the longest line, and gridHeight is most the amount of lines
        int gridWidth = 1, gridHeight;

        while (true) {
            List<ANSIMatch> ANSIMatches = new ArrayList<>();

            // Scan a line
            String line = reader.readLine();

            // No more to be read
            if (line == null) break;

            // Check the mode
            switch (parserState) {
                case COMMENTS:
                    parseCOMNT(line);
                    continue; // Skip the rest of the loop
                case CHARS:
                    if (line.length() > 5 && line.charAt(0) == SAUCE_CHAR) {
                        switch (line.substring(1, 6)) {
                            case "SAUCE":
                                parseSAUCE(line);
                                continue; // Skip the rest of the loop
                            case "COMNT":
                                parserState = ParserState.COMMENTS;
                                parseCOMNT(line);
                                continue; // Skip the rest of the loop
                        }
                    }
                    // Continue parsing as ANSI char mode
                    break;
            }

            // ANSIMatch sequences in this line
            Matcher ANSIMatcher = Patterns.ANSI8Bit.matcher(line);
            while (ANSIMatcher.find()) {
                ANSIMatches.add(new ANSIMatch(ANSIMatcher.start(), ANSIMatcher.end(), ANSIMatcher.group()));
            }

            // Use the ANSIMatches to filter ANSI sequences and characters into separate grids
            List<Character> charLine = new ArrayList<>();
            List<SGRSequence> SGRLine = new ArrayList<>();
            ANSIMatch nextANSIMatch = null;
            int lineCursor = 0, gridLineCursor = 0, currentMatchIndex = 0;
            while (lineCursor < line.length()) {
                // If there are still ANSIMatches to get, grab the next ANSIMatch
                if (currentMatchIndex < ANSIMatches.size()) {
                    nextANSIMatch = ANSIMatches.get(currentMatchIndex);
                }

                // If there's a match waiting, and the lineCursor has reached the start of it, do this
                if (nextANSIMatch != null && lineCursor == nextANSIMatch.start) {
                    SGRLine.add(gridLineCursor, new SGRSequence(nextANSIMatch.group));

                    gridLineCursor++;
                    lineCursor += nextANSIMatch.length;
                    currentMatchIndex++;

                    // Don't execute the character part below,
                    // instead just continue to the next iteration of this loop
                    continue;
                }

                // Just characters left
                charLine.add(gridLineCursor, line.charAt(lineCursor));

                gridLineCursor++;
                lineCursor++;
            }

            // Update gridWidth (longest line) if necessary
            // First figure out the longest line out of charLine and SGRLine
            int charLineAmount, SGRLineAmount, newWidth;
            charLineAmount = charLine.size();
            SGRLineAmount = SGRLine.size();
            if (charLineAmount >= SGRLineAmount) newWidth = charLineAmount;
            else newWidth = SGRLineAmount;
            // Then if the longest line is greater than the old gridWidth, then update it
            if (newWidth > gridWidth) gridWidth = newWidth;

            // Submit lists
            charLines.add(charLine);
            SGRLines.add(SGRLine);
        }
        // Remember to close the reader!
        reader.close();

        // Update gridHeight (most amount of lines)
        // First figure out which has the most lines out of charLines and SGRLines
        int charLinesAmount, SGRLinesAmount;
        charLinesAmount = charLines.size();
        SGRLinesAmount = SGRLines.size();
        // Then set gridHeight to the largest one
        if (charLinesAmount >= SGRLinesAmount) gridHeight = charLinesAmount;
        else gridHeight = SGRLinesAmount;

        // Set the sizes accordingly
        charGrid = new MutableGrid<>(new Size(gridWidth, gridHeight));
        SGRGrid = new MutableGrid<>(new Size(gridWidth, gridHeight));

        // Populate the charGrid
        int col = 0, row = 0;
        for (List<Character> charLine : charLines) {
            for (Character currentChar : charLine) {
                charGrid.set(col, row, Objects.requireNonNullElse(currentChar, ' '));
                col++;
            }
            row++;
        }
        // Populate the SGRGrid
        col = 0;
        row = 0;
        for (List<SGRSequence> SGRLine : SGRLines) {
            for (SGRSequence currentSGR : SGRLine) {
                if (currentSGR != null) {
                    SGRGrid.set(col, row, currentSGR);
                }
                col++;
            }
            row++;
        }
    }

    private void parseCOMNT(String line) {
        // Figure out where the metadata starts and ends
        int metaStart = getMetaStart(line), metaEnd = metaStart + 64;

        // Add comment
        tmpComments.add(line.substring(metaStart, metaEnd));

        // If there's more to the line
        if (line.length() - 1 > metaEnd + 5) { // +5 means it will discard anything without an ID, just to be sure
            metaStart = metaEnd + 1;
            switch (line.substring(metaStart, metaStart + 4)) {
                case "SAUCE":
                    parseSAUCE(line.substring(metaStart + 5));
                    break;
                case "COMNT":
                    parseCOMNT(line.substring(metaStart + 5));
                    break;
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

        public String getVersion() {
            return version;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getGroup() {
            return group;
        }

        public String getDate() {
            return date;
        }

        public String getFileSize() {
            return fileSize;
        }

        public String getDataType() {
            return dataType;
        }

        public String getFileType() {
            return fileType;
        }

        public String getTInfo1() {
            return TInfo1;
        }

        public String getTInfo2() {
            return TInfo2;
        }

        public String getTInfo3() {
            return TInfo3;
        }

        public String getTInfo4() {
            return TInfo4;
        }

        public String getCommentsAmount() {
            return commentsAmount;
        }

        public String getTFlags() {
            return TFlags;
        }

        public String getTInfoS() {
            return TInfoS;
        }
    }

    public MutableGrid<Character> getCharGrid() {
        return charGrid;
    }

    public MutableGrid<SGRSequence> getSGRGrid() {
        return SGRGrid;
    }

    public Sauce getSauce() {
        return sauce;
    }
}
