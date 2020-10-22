package net.accela.telnet.server;

import java.io.IOException;

/**
 * A response getting triggered after the {@link TelnetSessionServer}
 */
public interface TelnetResponse {
    /**
     * This may do any number of things, but the intended purpose of it is to
     * submit a reply to the {@link TelnetSessionServer} for further inquiry,
     * triggering other registered {@link TelnetResponse}'s.
     *
     * @throws IOException if there's an issue with IO, typically read/write related.
     */
    void run(TelnetSequence trigger) throws IOException;
}
