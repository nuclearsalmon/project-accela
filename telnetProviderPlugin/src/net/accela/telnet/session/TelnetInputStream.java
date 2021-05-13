package net.accela.telnet.session;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static net.accela.telnet.util.TelnetBytes.*;

/**
 * An InputStream for Telnet, filters out any telnet bytes and hands them to the
 * {@link TelnetNegotiator} for further parsing.
 */
public class TelnetInputStream extends InputStream {
    final TelnetNegotiator negotiator;
    final Socket socket;
    final InputStream inputStream;
    final byte[] buffer;
    final byte[] workingBuffer;
    int bytesInBuffer;

    TelnetInputStream(@NotNull TelnetNegotiator negotiator, @NotNull Socket socket) throws IOException {
        this.negotiator = negotiator;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.buffer = new byte[64 * 1024];
        this.workingBuffer = new byte[1024];
        this.bytesInBuffer = 0;
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException("TelnetInputStream doesn't support .read()");
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public int available() throws IOException {
        if (bytesInBuffer > 0) {
            return bytesInBuffer;
        }
        fillBuffer(false);
        return Math.abs(bytesInBuffer);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesInBuffer == -1) {
            return -1;
        }
        if (available() == 0) {
            // There was nothing in the buffer and the underlying
            // stream has nothing available, so do a blocking read
            // from the stream.
            fillBuffer(true);
        }
        if (bytesInBuffer <= 0) {
            return -1;
        }
        int bytesToCopy = Math.min(len, bytesInBuffer);
        System.arraycopy(buffer, 0, b, off, bytesToCopy);
        System.arraycopy(buffer, bytesToCopy, buffer, 0, buffer.length - bytesToCopy);
        bytesInBuffer -= bytesToCopy;
        return bytesToCopy;
    }

    private void fillBuffer(boolean block) throws IOException {
        int maxFill = Math.min(workingBuffer.length, buffer.length - bytesInBuffer);

        int oldTimeout = socket.getSoTimeout();
        if (!block) {
            socket.setSoTimeout(1);
        }
        int readBytes = inputStream.read(workingBuffer, 0, maxFill);
        if (!block) {
            socket.setSoTimeout(oldTimeout);
        }

        if (readBytes == -1) {
            bytesInBuffer = -1;
            return;
        }

        for (int i = 0; i < readBytes; i++) {
            if (workingBuffer[i] == IAC) {
                i++;
                switch (workingBuffer[i]) {
                    case DO:
                    case DONT:
                    case WILL:
                    case WONT:
                        parseCommand(workingBuffer, i, readBytes);
                        ++i;
                        continue;
                    case SB:
                        i += parseSubNegotiation(workingBuffer, ++i, readBytes);
                        continue;
                    default:
                        // Double IAC, let it be
                        if (workingBuffer[i] != IAC) {
                            System.err.println("Unknown Telnet command: " + workingBuffer[i]);
                        }
                        break;
                }
            }
            buffer[bytesInBuffer++] = workingBuffer[i];
        }
    }


    void parseCommand(byte[] buffer, int position, int max) throws IOException {
        if (position + 1 >= max) {
            throw new IllegalStateException("We got a command signal from the remote telnet client but "
                    + "not enough characters available in the stream");
        }
        byte command = buffer[position];
        byte option = buffer[position + 1];
        negotiator.parseSequence(new TelnetSequence(command, option));
    }

    int parseSubNegotiation(byte[] buffer, int position, int max) throws IOException {
        int originalPosition = position;

        // Read operation
        byte operation = buffer[position++];

        // Read until [IAC SE]
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        while (position < max) {
            byte read = buffer[position];
            if (read != IAC) {
                outputBuffer.write(read);
            } else {
                if (position + 1 == max) {
                    throw new IllegalStateException("Unexpected end of buffer when reading subnegotiation");
                }
                position++;
                // Escaped IAC
                if (buffer[position] == IAC) {
                    outputBuffer.write(IAC);
                } else if (buffer[position] == SE) {
                    negotiator.parseSequence(new TelnetSequence(SB, operation, outputBuffer.toByteArray()));
                    return ++position - originalPosition;
                }
            }
            position++;
        }
        throw new IllegalStateException("Unexpected end of buffer when reading subnegotiation, no IAC SE");
    }
}