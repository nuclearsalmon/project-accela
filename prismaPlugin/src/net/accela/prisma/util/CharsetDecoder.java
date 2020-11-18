package net.accela.prisma.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * This can decode with different {@link Charset}s that can be switched on the fly.
 * If the encoding gets switched in the middle of a reading (resulting in a corrupted character),
 * it will discard the previously read bytes.
 * <br>
 * Currently, it can decode UTF8 and various ASCII-based charsets (US_ASCII and CP437/IBM437).
 */
public abstract class CharsetDecoder {
    byte[] charBytes;
    // The number of bytes needed to form a character.
    int numberOfBytesNeeded;
    // The index of where the next byte will be written to the array. One ahead.
    int byteIndex;
    Charset oldCharset;

    public CharsetDecoder() {
        reset();
    }

    void reset() {
        charBytes = null;
        byteIndex = 0;
    }

    /**
     * Decodes one character, and returns once a single character has been decoded.
     * Loop this in order to decode more than one character.
     *
     * @return String for each character, otherwise null if nothing has been decoded yet.
     */
    public @Nullable String decodeByte(byte byteStringPart) {
        // Confirm that the charset hasn't changed - if it has,
        // then discard old byte array to prevent corruption
        Charset currentCharset = getCurrentCharset();
        if (oldCharset != currentCharset) reset();
        // Update old charset
        oldCharset = currentCharset;

        // If we don't have a character in the works, we first need to figure out how many bytes are needed for it
        if (charBytes == null) {
            if (currentCharset.equals(StandardCharsets.UTF_8)) {
                /*
                Binary    Hex          Comments
                0xxxxxxx  0x00..0x7F   Only byte of a 1-byte character encoding
                10xxxxxx  0x80..0xBF   Continuation byte: one of 1-3 bytes following the first
                110xxxxx  0xC0..0xDF   First byte of a 2-byte character encoding
                1110xxxx  0xE0..0xEF   First byte of a 3-byte character encoding
                11110xxx  0xF0..0xF7   First byte of a 4-byte character encoding
                */
                numberOfBytesNeeded = 1;
                if (byteStringPart >= (byte) 0xC0 && byteStringPart <= (byte) 0xDF) numberOfBytesNeeded = 2;
                else if (byteStringPart <= (byte) 0xEF) numberOfBytesNeeded = 3;
                else if (byteStringPart <= (byte) 0xF7) numberOfBytesNeeded = 4;
            } else if (currentCharset.equals(StandardCharsets.US_ASCII)
                    || currentCharset.equals(Charset.forName("IBM437"))) {
                numberOfBytesNeeded = 1;
            } else {
                throw new UnsupportedCharsetException(currentCharset.name());
            }

            // Make the new array
            charBytes = new byte[numberOfBytesNeeded];
        }

        // Add the byte we've got
        charBytes[byteIndex] = byteStringPart;
        byteIndex++;

        // Is the character completed? If yes, then process and reset.
        if (byteIndex == numberOfBytesNeeded) {
            String result = new String(charBytes, currentCharset);
            reset();
            return result;
        }

        return null;
    }

    /**
     * @return The currently used charset
     */
    protected abstract @NotNull Charset getCurrentCharset();
}
