package net.accela.telnet.old;

import net.accela.telnet.session.TelnetSequence;

import java.io.IOException;

public interface NegotiationHandler {
    /**
     * This should to react to the sequence if it matches, and otherwise ignore it.
     * @throws IOException If there's an issue with IO, typically read/write related.
     */
    void run(TelnetSequence sequence) throws IOException;
}
