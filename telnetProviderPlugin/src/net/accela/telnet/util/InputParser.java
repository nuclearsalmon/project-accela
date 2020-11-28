package net.accela.telnet.util;

import net.accela.ansi.AnsiLib;
import net.accela.ansi.Crayon;
import net.accela.prisma.PrismaWM;
import net.accela.prisma.event.InputEvent;
import net.accela.prisma.event.PointInputEvent;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.Size;
import net.accela.prisma.session.TextGraphicsSession;
import net.accela.prisma.util.InputEventParser;
import net.accela.server.AccelaAPI;
import net.accela.server.plugin.Plugin;
import net.accela.telnet.server.TelnetSessionServer;
import net.accela.telnet.session.TelnetSession;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Parses input and relays it to the sessions WindowManager, either as an Event or through an OutputStream,
 * depending on which one is preferred.
 */
// FIXME: 11/9/20 Make this into a separate thread to prevent freezes
//  note, this is no longer that big of an issue as callEvent
//  is now multithreaded and offhands tasks to hypervisor.
//  Still worth looking into however.
public class InputParser {
    final TelnetSession session;
    final Plugin plugin;
    final TelnetSessionServer sessionServer;
    final InputEventParser ansiParser;
    volatile CountDownLatch latch;

    public enum Mode {
        NORMAL,
        DETECT_SIZE,
        DETECT_UNICODE
    }

    @NotNull
    volatile Mode mode = Mode.NORMAL;

    public InputParser(@NotNull TelnetSession session, @NotNull TelnetSessionServer sessionServer) {
        this.session = session;
        this.sessionServer = sessionServer;
        this.plugin = session.getCreator().getPlugin();
        this.ansiParser = new InputEventParser(session.getCreator().getPlugin());
    }

    public void processDecoded(@NotNull String decoded) {
        // Try to turn it into an InputEvent
        InputEvent inputEvent = ansiParser.parse(decoded);
        if (inputEvent == null) return;

        // Send or parse the InputEvent
        switch (mode) {
            case NORMAL:
                // Check if the session has a WindowManager
                PrismaWM windowManager = session.getWindowManager();
                if (windowManager != null) {
                    session.getLogger().log(Level.INFO, "Calling event " + inputEvent);
                    AccelaAPI.getPluginManager().callEvent(inputEvent, windowManager.getBroadcastChannel());
                } else {
                    session.getLogger().log(Level.WARNING, "Engine down when trying to call an event via PluginManager");
                }
                break;
            case DETECT_SIZE:
                // If we got something, check if it's a PointInputEvent
                if (inputEvent instanceof PointInputEvent) {
                    PointInputEvent PointInputEvent = (PointInputEvent) inputEvent;

                    // Update the size
                    session.setTerminalSize(
                            new Size(PointInputEvent.getPoint().getX(), PointInputEvent.getPoint().getY())
                    );

                    // Change mode back to normal and unlock
                    reset();
                }
                break;
            case DETECT_UNICODE:
                // If we got something, check if it's a PointInputEvent.
                if (inputEvent instanceof PointInputEvent) {
                    PointInputEvent PointInputEvent = (PointInputEvent) inputEvent;

                    // See https://ryobbs.com/doku/terminal.php for info on the logic behind this.
                    // I probably butchered the idea... but my implementation seems to work *shrug*
                    session.setUnicodeSupport(PointInputEvent.getPoint().getX() == 7);

                    session.getLogger().log(Level.INFO, "point: " + PointInputEvent);

                    // Change mode back to normal and unlock
                    reset();
                }
                break;

        }
    }

    void reset() {
        // Change mode back to normal and unlock
        mode = Mode.NORMAL;

        // Count down
        latch.countDown();
        latch = null;
    }

    @NotNull
    public CountDownLatch updateTerminalSize() throws IOException {
        // New latch
        latch = new CountDownLatch(1);

        // Change mode
        mode = Mode.DETECT_SIZE;

        // Form a request
        String request =
                /* Move the cursor to the very bottom of the terminal */
                AnsiLib.CSI + "255B" +
                        /* Move the cursor to the very right of the terminal */
                        AnsiLib.CSI + "255C" +
                        /* Ask for the cursor location */
                        AnsiLib.CSI + "6n";

        // Send the request
        sessionServer.writeToClient(request.getBytes(session.getCharset()));

        return latch;
    }

    public void builtinUpdateTerminalSize() throws IOException, InterruptedException, NodeNotFoundException {
        System.out.println("Updating terminal size");

        CountDownLatch latch = updateTerminalSize();

        // Wait for the detection to complete, or time out
        latch.await(1000, TimeUnit.MILLISECONDS);

        System.out.println("Updated terminal size! New value: " + session.getTerminalSize());

        // Clear and redraw
        sessionServer.writeToClient(AnsiLib.CLR.getBytes(session.getCharset()));
        PrismaWM windowManager = session.getWindowManager();
        if (windowManager != null) windowManager.paint(new Rect(session.getTerminalSize()));
    }

    @NotNull
    public CountDownLatch updateUnicodeSupport() throws IOException {
        // New latch
        latch = new CountDownLatch(1);

        // Change to UTF-8 temporarily so that we can perform this detection
        session.setCharset(TextGraphicsSession.UTF8_CHARSET);

        // Change mode
        mode = Mode.DETECT_UNICODE;

        // Form a request
        String request =
                /* Clear to reset the cursor */
                AnsiLib.CLR +
                        /* Black FG and BG so that the characters aren't visible. Makes for a cleaner look */
                        new Crayon().blackBg(true).blackBg(false) +
                        /* Print three unicode characters that are 3 bytes in UTF-8 encoding */
                        "㐸惵㲒" +
                        /* Request to know where the cursor is now */
                        AnsiLib.CSI + "6n";

        // Send the request
        sessionServer.writeToClient(request.getBytes(session.getCharset()));

        return latch;
    }

    public void builtinUpdateUnicodeSupport() throws IOException, InterruptedException, NodeNotFoundException {
        System.out.println("Updating terminal size");

        Charset oldCharset = session.getCharset();
        CountDownLatch latch = updateUnicodeSupport();

        latch.await(1000, TimeUnit.MILLISECONDS);

        // Change the charset back to normal
        // If we DON'T support unicode, change the charset so that
        // CP437/IBM437 extended characters works.
        // If we DO support unicode, don't change anything, it's good as-is already.
        if (!session.getUnicodeSupport()) session.setCharset(TextGraphicsSession.IBM437_CHARSET);
        else session.setCharset(oldCharset);

        System.out.println("Updated unicode support! New value: " + session.getUnicodeSupport());

        // Clear and redraw
        sessionServer.writeToClient(AnsiLib.CLR.getBytes(session.getCharset()));
        PrismaWM windowManager = session.getWindowManager();
        if (windowManager != null) windowManager.paint(new Rect(session.getTerminalSize()));
    }
}
