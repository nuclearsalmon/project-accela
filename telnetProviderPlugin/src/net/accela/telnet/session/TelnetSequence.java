package net.accela.telnet.session;

import net.accela.telnet.exception.InvalidTelnetSequenceException;
import net.accela.telnet.util.TelnetBytes;

import java.util.Arrays;

import static net.accela.telnet.util.TelnetBytes.*;

public final class TelnetSequence {
    public final byte command;
    public final byte option;
    public final byte[] arguments;


    public TelnetSequence(byte command, byte option) {
        this(command, option, (byte[]) null);
    }

    public TelnetSequence(byte command, byte option, byte... arguments) {
        this.command = command;
        this.option = option;
        this.arguments = arguments;

        if (command == SB && (arguments == null || arguments.length <= 0)) {
            throw new InvalidTelnetSequenceException(String.format(
                    "Subnegotiation requires arguments. [cmd=%s, opt=%s, arg=%s]",
                    TelnetBytes.byteToString(command),
                    TelnetBytes.bytesToString(option),
                    TelnetBytes.bytesToString(arguments)
            ));
        }

        if (command != SB && (arguments != null && arguments.length > 0)) {
            throw new InvalidTelnetSequenceException(String.format(
                    "Non-subnegotiation should not have arguments. [cmd=%s, opt=%s, arg=%s]",
                    TelnetBytes.byteToString(command),
                    TelnetBytes.bytesToString(option),
                    TelnetBytes.bytesToString(arguments)
            ));
        }
    }

    public byte getCommand() {
        return command;
    }

    public byte getOption() {
        return option;
    }

    public byte[] getArguments() {
        return arguments;
    }

    /**
     * Prefixes with IAC and combines all the bytes present into a sequence.
     * Suffixes any SB statement with IAC SE.
     *
     * @return The byte representation of this telnet sequence.
     */
    public byte[] getByteSequence() throws InvalidTelnetSequenceException {
        byte[] sequence;
        if (command == SB) {
            sequence = new byte[3 + arguments.length + 2];
            sequence[0] = IAC;
            sequence[1] = command;
            sequence[2] = option;
            // make into system.arraycopy
            for (int i = 3; i < sequence.length; i++) {
                sequence[i] = arguments[i - 3];
            }
            sequence[sequence.length - 2] = IAC;
            sequence[sequence.length - 1] = SE;
        } else {
            sequence = new byte[3];
            sequence[0] = IAC;
            sequence[1] = command;
            sequence[2] = option;
        }
        return sequence;
    }

    @Override
    public String toString() {
        return TelnetBytes.bytesToString(getByteSequence());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getByteSequence());
    }
}
