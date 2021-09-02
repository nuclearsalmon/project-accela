package net.accela.prismatic.input;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * The {@link MultiCharsetReader} acts much like {@link java.io.InputStreamReader},
 * except that the decoding charset can be switched on the fly.
 * <p>
 * This allows it to be used in network communication protocols
 * that switch charset after client/server negotiation.
 * <p>
 * It is based off of a hacked-together version of
 * {@link InputStreamReader} and Sun's {@code StreamDecoder}.
 */
public class MultiCharsetReader extends Reader {
    public static final int BYTE_BUFFER_SIZE = 8192;
    public static final int CHAR_BUFFER_SIZE = 8192;

    private final ByteBuffer byteBuffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
    private final CharBuffer charBuffer = CharBuffer.allocate(CHAR_BUFFER_SIZE);
    private final Object lock = new Object();

    private final InputStream inputStream;
    private volatile CharsetDecoder decoder;
    private volatile boolean isOpen = true;

    public MultiCharsetReader(final @NotNull InputStream inputStream,
                              final @NotNull Charset charset) {
        super(inputStream);
        this.inputStream = inputStream;
        this.decoder = createDecoder(charset);
    }

    //
    // Charset operations
    //

    public Charset getCharset() {
        return decoder.charset();
    }

    public synchronized void setCharset(@NotNull Charset charset) {
        synchronized (this) {
            flush();
            this.decoder = createDecoder(charset);
        }
    }

    private @NotNull CharsetDecoder createDecoder(@NotNull Charset charset) {
        return charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    //
    // Reading
    //

    @Override
    public synchronized int read() throws IOException {
        synchronized (lock) {
            // Return the leftover char, if there is one
            if (hasLeftoverChar()) return charBuffer.get();

            // Convert more bytes
            char[] cb = new char[2];
            int n = this.read(cb, 0, 2);
            switch (n) {
                case -1:
                    return -1;
                case 2:
                    charBuffer.append(cb[1]);
                    // FALL THROUGH
                case 1:
                    return cb[0];
                default:
                    assert false : n;
                    return -1;
            }
        }
    }

    @Override
    public synchronized int read(char @NotNull [] cbuf, int offset, int length) throws IOException {
        int off = offset;
        int len = length;
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0)
                return 0;

            int n = 0;

            if (hasLeftoverChar()) {
                // Copy the leftover char into the buffer
                cbuf[off] = charBuffer.get();
                off++;
                len--;
                n = 1;
                if ((len == 0) || !implReady())
                    // Return now if this is all we can produce w/o blocking
                    return n;
            }

            if (len == 1) {
                // Treat single-character array reads just like read()
                int c = read();
                if (c == -1)
                    return (n == 0) ? -1 : n;
                cbuf[off] = (char) c;
                return n + 1;
            }

            return n + implRead(cbuf, off, off + len);
        }
    }

    private int readBytes() throws IOException {
        byteBuffer.compact();
        try {
            // Read from the input stream, and then update the buffer
            int lim = byteBuffer.limit();
            int pos = byteBuffer.position();
            //assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            assert rem > 0;
            int n = inputStream.read(byteBuffer.array(), byteBuffer.arrayOffset() + pos, rem);
            if (n < 0)
                return n;
            if (n == 0)
                throw new IOException("Underlying input stream returned zero bytes");
            assert (n <= rem) : "n = " + n + ", rem = " + rem;
            byteBuffer.position(pos + n);
        } finally {
            // Flip even when an IOException is thrown,
            // otherwise the stream will stutter
            byteBuffer.flip();
        }

        int rem = byteBuffer.remaining();
        assert (rem != 0) : rem;
        return rem;
    }

    int implRead(char[] cbuf, int off, int end) throws IOException {

        // In order to handle surrogate pairs, this method requires that
        // the invoker attempt to read at least two characters.  Saving the
        // extra character, if any, at a higher level is easier than trying
        // to deal with it here.
        assert (end - off > 1);

        CharBuffer charBufferWrap = CharBuffer.wrap(cbuf, off, end - off);
        if (charBufferWrap.position() != 0)
            // Ensure that cb[0] == cbuf[off]
            charBufferWrap = charBufferWrap.slice();

        boolean eof = false;
        while (true) {
            CoderResult cr = decoder.decode(byteBuffer, charBufferWrap, eof);
            if (cr.isUnderflow()) {
                if (eof)
                    break;
                if (!charBufferWrap.hasRemaining())
                    break;
                if ((charBufferWrap.position() > 0) && !inputStreamReady())
                    break;          // Block at most once
                int n = readBytes();
                if (n < 0) {
                    eof = true;
                    if ((charBufferWrap.position() == 0) && (!byteBuffer.hasRemaining()))
                        break;
                    decoder.reset();
                }
                continue;
            }
            if (cr.isOverflow()) {
                assert charBufferWrap.position() > 0;
                break;
            }
            cr.throwException();
        }

        if (eof) {
            // Need to flush decoder
            decoder.reset();
        }

        if (charBufferWrap.position() == 0) {
            if (eof)
                return -1;
            assert false;
        }
        return charBufferWrap.position();
    }

    //
    // Readiness
    //

    private boolean inputStreamReady() {
        try {
            return inputStream.available() > 0;
        } catch (IOException x) {
            return false;
        }
    }

    boolean implReady() {
        return byteBuffer.hasRemaining() || inputStreamReady();
    }

    @Override
    public synchronized boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();
            return hasLeftoverChar() || implReady();
        }
    }

    //
    // Internals, flushing and closing
    //

    private boolean hasLeftoverChar() {
        return charBuffer.hasRemaining();
    }

    private void ensureOpen() throws IOException {
        if (!isOpen) throw new IOException("Stream closed");
    }

    private void flush() {
        decoder.decode(byteBuffer, charBuffer, true);
        decoder.flush(charBuffer);
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public synchronized void close() throws IOException {
        synchronized (lock) {
            if (!isOpen) return;
            flush();
            inputStream.close();
            isOpen = false;
        }
    }
}
