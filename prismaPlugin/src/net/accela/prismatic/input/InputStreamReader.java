package net.accela.prismatic.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Derived from decompiled classes and various github findings.
 * It's not actually used in the code (hence the @Deprecated), but it gets to stay as a reference piece.
 * It was used as reference when making {@link StreamDecoder} and {@link SwitchableCharsetReader}.
 */
@Deprecated
public class InputStreamReader extends Reader {
    private final StreamDecoder sd;

    /**
     * Creates an InputStreamReader that uses the default charset.
     *
     * @param in An InputStream
     */
    public InputStreamReader(InputStream in) {
        super(in);
        sd = StreamDecoder.forInputStreamReader(in, this, Charset.defaultCharset()); // ## check lock object
    }

    /**
     * Creates an InputStreamReader that uses the named charset.
     *
     * @param in          An InputStream
     * @param charsetName The name of a supported
     *                    {@link java.nio.charset.Charset charset}
     * @throws UnsupportedEncodingException If the named charset is not supported
     */
    public InputStreamReader(InputStream in, String charsetName)
            throws UnsupportedEncodingException {
        super(in);
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        sd = StreamDecoder.forInputStreamReader(in, this, charsetName);
    }

    /**
     * Creates an InputStreamReader that uses the given charset.
     *
     * @param in An InputStream
     * @param cs A charset
     * @since 1.4
     */
    public InputStreamReader(InputStream in, Charset cs) {
        super(in);
        if (cs == null)
            throw new NullPointerException("charset");
        sd = StreamDecoder.forInputStreamReader(in, this, cs);
    }

    /**
     * Creates an InputStreamReader that uses the given charset decoder.
     *
     * @param in  An InputStream
     * @param dec A charset decoder
     * @since 1.4
     */
    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        if (dec == null)
            throw new NullPointerException("charset decoder");
        sd = StreamDecoder.forInputStreamReader(in, this, dec);
    }

    /**
     * Returns the name of the character encoding being used by this stream.
     *
     * <p> If the encoding has an historical name then that name is returned;
     * otherwise the encoding's canonical name is returned.
     *
     * <p> If this instance was created with the {@link
     * #InputStreamReader(InputStream, String)} constructor then the returned
     * name, being unique for the encoding, may differ from the name passed to
     * the constructor. This method will return {@code null} if the
     * stream has been closed.
     * </p>
     *
     * @return The historical name of this encoding, or
     * {@code null} if the stream has been closed
     * @revised 1.4
     * @see java.nio.charset.Charset
     */
    public String getEncoding() {
        return sd.getEncoding();
    }

    /**
     * Reads a single character.
     *
     * @return The character read, or -1 if the end of the stream has been
     * reached
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException {
        return sd.read();
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param cbuf   Destination buffer
     * @param offset Offset at which to start storing characters
     * @param length Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the
     * stream has been reached
     * @throws IOException               If an I/O error occurs
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public int read(char[] cbuf, int offset, int length) throws IOException {
        return sd.read(cbuf, offset, length);
    }

    /**
     * Tells whether this stream is ready to be read.  An InputStreamReader is
     * ready if its input buffer is not empty, or if bytes are available to be
     * read from the underlying byte stream.
     *
     * @throws IOException If an I/O error occurs
     */
    public boolean ready() throws IOException {
        return sd.ready();
    }

    public void close() throws IOException {
        sd.close();
    }
}