package net.accela.telnet.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * Same as {@link CharsetDecoder}, but as a {@link Runnable} and reading from an {@link InputStream}.
 *
 * @see CharsetDecoder
 */
public abstract class CharsetStreamDecoder extends CharsetDecoder implements Runnable {
    @NotNull
    final InputStream inputStream;

    public CharsetStreamDecoder(@NotNull InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public synchronized void run() {
        Thread.currentThread().setName("CharsetStreamDecoder");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Read a byte
                int by = inputStream.read();

                // Always check for disconnect
                if (by == -1) throw new InterruptedIOException("Stream ended");

                // Decode it
                String result = decodeByte((byte) by);
                if (result != null) processDecoded(result);
            }
        } catch (InterruptedIOException ignored) {
            // Don't do anything
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    /**
     * Do something with resulting string from the decoded bytes
     *
     * @param decoded The resulting string from the decoded bytes. Null if nothing was decoded (yet).
     */
    protected abstract void processDecoded(@NotNull String decoded);
}
