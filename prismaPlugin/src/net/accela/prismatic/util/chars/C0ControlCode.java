package net.accela.prismatic.util.chars;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum C0ControlCode {
    /**
     * Null (^@, \0)<br><br>
     * <p>
     * Originally used to allow gaps to be left on paper tape for edits.
     * Later used for padding after a code that might take a terminal
     * some time to process (e.g. a carriage return or line feed on a printing terminal).
     * Now often used as a string terminator, especially in the programming language C.
     */
    NUL((byte) 0, "^@"),

    /**
     * Start of Heading (^A)<br><br>
     * <p>
     * In message transmission, delimits the start of a message header.
     * The format of this header may be defined by an applicable protocol,
     * such as IPTC 7901 for journalistic text transmission,
     * and it is usually terminated by STX. In Hadoop,
     * it is often used as a field separator.
     */
    SOH((byte) 1, "^A"),

    /**
     * Start of Text (^B)<br><br>
     * <p>
     * First character of message text, and may be used to terminate the message heading.
     *
     * @see #ETX
     */
    STX((byte) 2, "^B"),

    /**
     * End of Text (^C)<br><br>
     * <p>
     * In message transmission, delimits the end of the main text of a message.
     * Might be followed by "post-text information" (i.e. a structured footer)
     * defined by an applicable protocol[2] or by any additional texts, followed by EOT.
     * In keyboard input, often used as a "break" character (Ctrl-C) to
     * interrupt or terminate a program or process.
     *
     * @see #STX
     */
    ETX((byte) 3, "^C"),

    /**
     * End of Transmission (^D)<br><br>
     * <p>
     * Delimits the end of a transmitted message,
     * which may include a header, message text and post-text footer,
     * or even multiple texts and associated headings.
     * May also be used to place terminals on standby.
     * Often used on Unix to indicate end-of-file on a terminal.
     */
    EOT((byte) 4, "^D"),

    /**
     * Enquiry (^E)<br><br>
     * <p>
     * Signal intended to trigger a response at the receiving end, to see if it is still present.
     */
    ENQ((byte) 5, "^E"),

    /**
     * Acknowledge (^F)<br><br>
     * <p>
     * Response to an ENQ, or an indication of successful receipt of a message.
     *
     * @see #NAK
     */
    ACK((byte) 6, "^F"),

    /**
     * Bell, Alert (^G, \a)<br><br>
     * <p>
     * Originally used to sound a bell on the terminal.
     * Later used for a beep on systems that didn't have a physical bell.
     * May also quickly turn on and off inverse video (a visual bell).
     */
    BEL((byte) 7, "^G"),

    /**
     * Backspace (^H, \b)<br><br>
     * <p>
     * Move the cursor one position leftwards.
     * On input, this may delete the character to the left of the cursor.
     * On output, where in early computer technology a character once printed could not be erased,
     * the backspace was sometimes used to generate accented characters in ASCII.
     * For example, <b><code>à</code></b> could be produced using the
     * three character sequence <b><code>a BS `</code></b>
     * (or, using the characters’ hex values, 0x61 0x08 0x60).
     * This usage is now generally not supported (it is prohibited in, for instance, ISO/IEC 8859).
     * To provide disambiguation between the two potential uses of backspace,
     * the cancel character control code was made part of the standard C1 control set.
     *
     * @see #DEL
     * DEL acts as backspace on some keyboards
     */
    BS((byte) 8, "^H"),

    /**
     * Horizontal Tabulation, Character Tabulation (^I, \t)<br><br>
     * <p>
     * Position to the next character tab stop.
     *
     * @see #VT
     */
    HT((byte) 9, "^I"),

    /**
     * Line Feed (^J, \n)<br><br>
     * <p>
     * On typewriters, printers, and some terminal emulators,
     * moves the cursor down one row without affecting its column position.
     * On Unix, used to mark end-of-line.
     * In DOS, Windows, and various network standards,
     * LF is used following CR as part of the end-of-line mark.
     *
     * @see #CR
     */
    LF((byte) 10, "^J"),

    /**
     * Vertical Tabulation, Line Tabulation (^K, \v)<br><br>
     * <p>
     * Position the form at the next line tab stop.
     *
     * @see #HT
     */
    VT((byte) 11, "^K"),

    /**
     * Form Feed (^L, \f)<br><br>
     * <p>
     * On printers, load the next page. Treated as whitespace in many programming languages,
     * and may be used to separate logical divisions in code.
     * In some terminal emulators, it clears the screen.
     * It still appears in some common plain text files as a page break character,
     * such as the RFCs published by IETF.
     */
    FF((byte) 12, "^L"),

    /**
     * Carriage Return (^M, \r)<br><br>
     * <p>
     * Originally used to move the cursor to column zero while staying on the same line.
     * On classic Mac OS (pre-Mac OS X), as well as in earlier systems such as
     * the Apple II and Commodore 64, used to mark end-of-line. In DOS, Windows,
     * and various network standards, it is used preceding LF as part of the end-of-line mark.
     * The Enter or Return key on a keyboard will send this character,
     * but it may be converted to a different end-of-line sequence by a terminal program.
     *
     * @see #LF
     */
    CR((byte) 13, "^M"),

    /**
     * Shift Out (^N)<br><br>
     * <p>
     * Switch to an alternative character set.
     *
     * @see #SI
     */
    SO((byte) 14, "^N"),

    /**
     * Shift In (^O)<br><br>
     * <p>
     * Return to regular character set after Shift Out.
     *
     * @see #SO
     */
    SI((byte) 15, "^O"),

    /**
     * Data Link Escape (^P)<br><br>
     * <p>
     * Cause a limited number of contiguously following octets to be interpreted in some different way,
     * for example as raw data (as opposed to control codes or graphic characters).
     * The details of this are implementation dependent.
     * Standards such as (the now-withdrawn) ECMA-37 existed for specific applications
     * of the Data Link Escape character for accessing additional transmission control functions.
     * Standard Compression Scheme for Unicode suggests replacing all C0-range bytes with DLE,
     * followed by that byte plus 0x40, if SCSU data must be transmitted over a system
     * which would be confused by SCSU's repurposing of the C0 bytes.
     */
    DLE((byte) 16, "^P"),

    /**
     * Device Control One - XON (^Q)<br><br>
     * <p>
     * Reserved for device control, with the interpretation dependent upon the device
     * to which they were connected.
     * <p>
     * DC1 and DC2 were intended primarily to indicate activating a device.
     * <p>
     * DC1 and DC3 (known also as XON and XOFF respectively in this usage) originated
     * as the "start and stop remote paper-tape-reader" functions in ASCII Telex networks.
     * This teleprinter usage became the de facto standard for software flow control.
     *
     * @see #DC2
     * @see #DC3
     * @see #DC4
     */
    DC1((byte) 17, "^Q"),

    /**
     * Device Control Two (^R)<br><br>
     * <p>
     * Reserved for device control, with the interpretation dependent upon the device
     * to which they were connected.
     * <p>
     * DC1 and DC2 were intended primarily to indicate activating a device.
     *
     * @see #DC1
     * @see #DC3
     * @see #DC4
     */
    DC2((byte) 18, "^R"),

    /**
     * Device Control Three - XOFF (^S)<br><br>
     * <p>
     * Reserved for device control, with the interpretation dependent upon the device
     * to which they were connected.
     * <p>
     * DC3 and DC4 were intended primarily to indicate pausing or turning off a device.
     * <p>
     * DC1 and DC3 (known also as XON and XOFF respectively in this usage) originated
     * as the "start and stop remote paper-tape-reader" functions in ASCII Telex networks.
     * This teleprinter usage became the de facto standard for software flow control.
     *
     * @see #DC1
     * @see #DC2
     * @see #DC4
     */
    DC3((byte) 19, "^S"),

    /**
     * Device Control Four (^T)<br><br>
     * <p>
     * Reserved for device control, with the interpretation dependent upon the device
     * to which they were connected.
     * <p>
     * DC3 and DC4 were intended primarily to indicate pausing or turning off a device.
     *
     * @see #DC1
     * @see #DC2
     * @see #DC3
     */
    DC4((byte) 20, "^T"),

    /**
     * Negative Acknowledge	(^U)<br><br>
     * <p>
     * Sent by a station as a negative response to the station with which the connection has been set up.
     * In binary synchronous communication protocol,
     * the NAK is used to indicate that an error was detected in the previously received block
     * and that the receiver is ready to accept retransmission of that block.
     * In multipoint systems, the NAK is used as the not-ready reply to a poll.
     *
     * @see #ACK
     */
    NAK((byte) 21, "^U"),

    /**
     * Synchronous Idle (^V)<br><br>
     * <p>
     * Used in synchronous transmission systems to provide a signal
     * from which synchronous correction may be achieved between data terminal equipment,
     * particularly when no other character is being transmitted.
     */
    SYN((byte) 22, "^V"),

    /**
     * End of Transmission Block (^W)<br><br>
     * <p>
     * Indicates the end of a transmission block of data
     * when data are divided into such blocks for transmission purposes.
     * If it is not in use for another purpose,
     * IPTC 7901 recommends interpreting ETB as an end of paragraph character.
     */
    ETB((byte) 23, "^W"),

    /**
     * Cancel (^X)<br><br>
     * <p>
     * Indicates that the data preceding it are in error or are to be disregarded.
     */
    CAN((byte) 24, "^X"),

    /**
     * End of Medium (^Y)<br><br>
     * <p>
     * Intended as means of indicating on paper or magnetic tapes that the end of the usable portion
     * of the tape had been reached. It might also mark the end of the used portion of the medium,
     * and does not necessarily correspond to the physical end of medium.
     * If it is not in use for another purpose, IPTC 7901 recommends repurposing EM
     * as an em space for indenting the first line of a paragraph (see also EMSP).
     */
    EM((byte) 25, "^Y"),

    /**
     * Substitute (^Z)<br><br>
     * <p>
     * Originally intended for use as a transmission control character
     * to indicate that garbled or invalid characters had been received.
     * It has often been put to use for other purposes when
     * the in-band signaling of errors it provides is unneeded,
     * especially where robust methods of error detection and correction are used,
     * or where errors are expected to be rare enough to make using the character
     * for other purposes advisable. In DOS, Windows, CP/M,
     * and other derivatives of Digital Equipment Corporation operating systems,
     * it is used to indicate the end of file, both when typing on the terminal,
     * and sometimes in text files stored on disk.
     */
    SUB((byte) 26, "^Z"),

    /**
     * Escape (^[, \e)<br><br>
     * <p>
     * The Esc key on the keyboard will cause this character to be sent on most systems.
     * It can be used in software user interfaces to exit from a screen, menu, or mode,
     * or in device-control protocols (e.g., printers and terminals) to signal
     * that what follows is a special command sequence rather than normal text.
     * In systems based on ISO/IEC 2022, even if another set of C0 control codes are used,
     * this octet is required to always represent the escape character.
     */
    ESC((byte) 27, "^["),

    /**
     * File Separator (^\)<br><br>
     * <p>
     * Can be used as delimiters to mark fields of data structures.
     * If used for hierarchical levels, US is the lowest level (dividing plain-text data items),
     * while RS, GS, and FS are of increasing level to divide groups made up of items of the level beneath it.
     * The Unix info format uses US, followed by an optional form-feed and a line break,
     * to mark the beginning of a node.
     * MARC 21 uses US as a subfield delimiter, RS as a field terminator and GS as a record terminator.
     * In the current edition of IPTC 7901, if they are not used for other purposes,
     * US is recommended for use as a column separator in tables, FS as a "Central Field Separator" in tables,
     * and GS and RS respectively for marking a following space or hyphen-minus as non-breaking or soft respectively
     * (in character sets not supplying explicit NBSP and SHY characters).
     * Python's splitLines string method treats FS, GS and RS, but not US,
     * as separators in addition to the line-breaking characters.
     */
    FS((byte) 28, "^\\"),

    /**
     * Group Separator (^])<br><br>
     * <p>
     * Can be used as delimiters to mark fields of data structures.
     * If used for hierarchical levels, US is the lowest level (dividing plain-text data items),
     * while RS, GS, and FS are of increasing level to divide groups made up of items of the level beneath it.
     * The Unix info format uses US, followed by an optional form-feed and a line break,
     * to mark the beginning of a node.
     * MARC 21 uses US as a subfield delimiter, RS as a field terminator and GS as a record terminator.
     * In the current edition of IPTC 7901, if they are not used for other purposes,
     * US is recommended for use as a column separator in tables, FS as a "Central Field Separator" in tables,
     * and GS and RS respectively for marking a following space or hyphen-minus as non-breaking or soft respectively
     * (in character sets not supplying explicit NBSP and SHY characters).
     * Python's splitLines string method treats FS, GS and RS, but not US,
     * as separators in addition to the line-breaking characters.
     */
    GS((byte) 29, "^]"),

    /**
     * Record Separator (^^)<br><br>
     * <p>
     * Can be used as delimiters to mark fields of data structures.
     * If used for hierarchical levels, US is the lowest level (dividing plain-text data items),
     * while RS, GS, and FS are of increasing level to divide groups made up of items of the level beneath it.
     * The Unix info format uses US, followed by an optional form-feed and a line break,
     * to mark the beginning of a node.
     * MARC 21 uses US as a subfield delimiter, RS as a field terminator and GS as a record terminator.
     * In the current edition of IPTC 7901, if they are not used for other purposes,
     * US is recommended for use as a column separator in tables, FS as a "Central Field Separator" in tables,
     * and GS and RS respectively for marking a following space or hyphen-minus as non-breaking or soft respectively
     * (in character sets not supplying explicit NBSP and SHY characters).
     * Python's splitLines string method treats FS, GS and RS, but not US,
     * as separators in addition to the line-breaking characters.
     */
    RS((byte) 30, "^^"),

    /**
     * Unit Separator (^_)<br><br>
     * <p>
     * <p>
     * Can be used as delimiters to mark fields of data structures.
     * If used for hierarchical levels, US is the lowest level (dividing plain-text data items),
     * while RS, GS, and FS are of increasing level to divide groups made up of items of the level beneath it.
     * The Unix info format uses US, followed by an optional form-feed and a line break,
     * to mark the beginning of a node.
     * MARC 21 uses US as a subfield delimiter, RS as a field terminator and GS as a record terminator.
     * In the current edition of IPTC 7901, if they are not used for other purposes,
     * US is recommended for use as a column separator in tables, FS as a "Central Field Separator" in tables,
     * and GS and RS respectively for marking a following space or hyphen-minus as non-breaking or soft respectively
     * (in character sets not supplying explicit NBSP and SHY characters).
     * Python's splitLines string method treats FS, GS and RS, but not US,
     * as separators in addition to the line-breaking characters.
     */
    US((byte) 31, "^_"),

    //
    // While not technically part of the C0 control character range,
    // the following two characters are defined in ISO/IEC 2022
    // as always being available regardless of which sets of control characters
    // and graphics characters have been registered.
    // They can be thought of as having some characteristics of control characters.
    //

    /**
     * Space ( )<br><br>
     * <p>
     * Space is a graphic character. It has a visual representation consisting of the absence of a graphic symbol.
     * It causes the active position to be advanced by one character position. In some applications,
     * Space can be considered a lowest-level "word separator" to be used with the adjacent separator characters.
     *
     * @see #DEL
     */
    SP((byte) 32, " "),

    /**
     * Delete (^?)<br><br>
     *
     * <b>NOTE: This is NOT the delete key.</b><br>
     * <p>
     * Not technically part of the C0 control character range,
     * this was originally used to mark deleted characters on paper tape,
     * since any character could be changed to all ones by punching holes everywhere.
     * On VT100 compatible terminals, this is the character generated by the key labelled ⌫,
     * usually called backspace on modern machines, and does not correspond to the PC delete key.
     *
     * @see #BS
     * @see #SP
     */
    DEL((byte) 127, "^?"),
    ;

    //
    // Constructor and methods
    //

    private final static Map<@NotNull Byte, @NotNull C0ControlCode> byteMap = new HashMap<>();

    private final byte by;
    private final @NotNull String caretNotation;

    C0ControlCode(byte by, @NotNull String caretNotation) {
        this.by = by;
        this.caretNotation = caretNotation.intern();
    }

    public byte getByte() {
        return by;
    }

    public char getChar() {
        return (char) getByte();
    }

    public @NotNull String getCaretNotation() {
        return caretNotation;
    }

    /**
     * @param by The byte to look for
     * @return The matching {@link C0ControlCode}, or null if not found.
     */
    public static @Nullable C0ControlCode fromByte(byte by) {
        // Populate the hashmap if that's not been dealt with already.
        synchronized (byteMap) {
            if (byteMap.size() < 1) {
                for (C0ControlCode value : values()) {
                    byteMap.put(value.getByte(), value);
                }
            }
        }

        // Return the SGR at this index
        return byteMap.get(by);
    }

    // todo implement

    /**
     * Several of the basic ASCII control codes are classified into a few categories,
     * and sometimes given alternative abbreviated names consisting of that category and a number
     */
    public enum Category {
        TRANSMISSION_CONTROLS,
        FORMAT_EFFECTORS,
        DEVICE_CONTROLS,
        INFORMATION_SEPARATORS,
        LOCKING_SHIFTS,
        OTHERS
    }
}
