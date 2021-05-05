package net.accela.prismatic;

import net.accela.prismatic.annotation.Container;
import net.accela.prismatic.behaviour.MouseCaptureMode;
import net.accela.prismatic.event.ActivationEvent;
import net.accela.prismatic.event.TerminalResizeEvent;
import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.DrawableContainer;
import net.accela.prismatic.gui.drawabletree.Branch;
import net.accela.prismatic.gui.drawabletree.DrawableTree;
import net.accela.prismatic.gui.drawabletree.Node;
import net.accela.prismatic.gui.drawabletree.NodeNotFoundException;
import net.accela.prismatic.gui.geometry.Point;
import net.accela.prismatic.gui.geometry.Rect;
import net.accela.prismatic.gui.geometry.Size;
import net.accela.prismatic.gui.geometry.exception.RectOutOfBoundsException;
import net.accela.prismatic.gui.text.BasicTextGrid;
import net.accela.prismatic.gui.text.TextCharacter;
import net.accela.prismatic.gui.text.TextGrid;
import net.accela.prismatic.gui.text.color.TextColor;
import net.accela.prismatic.gui.text.effect.TextEffect;
import net.accela.prismatic.input.lanterna.actions.KeyStroke;
import net.accela.prismatic.session.TextGraphicsSession;
import net.accela.prismatic.terminal.AbstractTerminal;
import net.accela.prismatic.terminal.ModernTerminal;
import net.accela.server.AccelaAPI;
import net.accela.server.event.Event;
import net.accela.server.event.EventChannel;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class Prismatic implements Container, Closeable {
    /**
     * The session hosting this
     */
    final TextGraphicsSession session;

    /**
     * The plugin instance of {@link Prismatic}
     */
    protected final Plugin pluginInstance;

    /**
     * The {@link EventChannel} for this instance
     */
    public final EventChannel broadcastChannel = new EventChannel();

    boolean isClosed = false;

    /**
     * The {@link DrawableTree} representing all {@link Drawable}s attached to {@link Prismatic}
     */
    final DrawableTree tree = new DrawableTree(this);

    final BroadcastListener broadcastListener = new BroadcastListener();

    MouseCaptureMode mouseCaptureMode = null;

    final Lock sessionWriteLock = new ReentrantLock();
    final Lock broadcastLock = new ReentrantLock();

    final BasicTextGrid backBuffer;
    final BasicTextGrid frontBuffer;

    private Size previousTerminalSize;

    public Prismatic(@NotNull TextGraphicsSession session) throws IOException {
        this.session = session;
        previousTerminalSize = getTerminal().getSize();
        this.backBuffer = new BasicTextGrid(previousTerminalSize);
        this.frontBuffer = new BasicTextGrid(previousTerminalSize);

        // Get plugin instance
        pluginInstance = Main.getPluginInstance();
        if (pluginInstance == null) throw new IllegalStateException("Missing plugin instance");

        new Thread() {
            @Override
            public void run() {
                String closeReason = "Unknown";
                while (!isInterrupted()) {
                    KeyStroke keyStroke = null;
                    try {
                        keyStroke = getTerminal().pollInput();
                        if (keyStroke == null) continue;
                        receiveEvent(keyStroke);
                    } catch (IOException ex) {
                        closeReason = "IOException";
                        ex.printStackTrace();
                        break;
                    }
                }
                close();
                session.close(closeReason);
            }
        }.start();

        // Enter private mode and clear it
        getTerminal().enterPrivateMode();
        getTerminal().resetColorAndSGR();
        getTerminal().clear();

        // Register event listeners
        AccelaAPI.getPluginManager().registerEvents(broadcastListener, pluginInstance, broadcastChannel);

        // Enable mouse
        // Cycle through modes so that at least one of them will get applied.
        // We don't know what the terminal supports so it's best to be on the safe side of things.
        getTerminal().setMouseCaptureMode(MouseCaptureMode.CLICK);
        getTerminal().setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE);
        getTerminal().setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);
    }

    public @NotNull EventChannel getBroadcastChannel() {
        return broadcastChannel;
    }

    //
    // Container methods
    //

    /**
     * Attaches a {@link Drawable} to this {@link Container}
     *
     * @param drawable The {@link Drawable} to attach
     * @throws RectOutOfBoundsException when the rect is invalid
     */
    public void attach(@NotNull Drawable drawable, @NotNull Plugin plugin)
            throws RectOutOfBoundsException, NodeNotFoundException, IOException {
        // Checks
        checkClosed();
        if (Main.DBG_RESPECT_TERMINAL_BOUNDS &&
                !Rect.contains(new Rect(getTerminal().getSize()), drawable.getRelativeRect())) {
            throw new RectOutOfBoundsException("Drawable does not fit within the terminal");
        }

        // Attach
        tree.newNode(drawable, plugin);

        // Register any events
        AccelaAPI.getPluginManager().registerEvents(drawable, drawable.findPlugin(), drawable.getChannel());

        // Focus
        if (Main.DBG_FOCUS_ON_WM_ATTACHMENT) {
            receiveEvent(new ActivationEvent(pluginInstance, drawable.getIdentifier()));

            // Show/Hide cursor
            //todo also move cursor
            getTerminal().setCursorVisible(drawable.getCursorMode() == Drawable.CursorMode.TERMINAL_RENDERED);
        }
    }

    /**
     * Detaches a {@link Drawable} from this {@link Container}
     */
    public void detach(@NotNull Drawable drawable) throws NodeNotFoundException, IOException {
        pluginInstance.getLogger().log(Level.INFO, "Detaching drawable '" + drawable + "'");

        // Get the rect before detaching, we're going to need it later
        Rect rect = drawable.getAbsoluteRect();

        // Detach
        Node node = DrawableTree.getNode(drawable);
        if (node != null) node.kill();

        // Redraw the now empty rect
        paint(rect);

        // Attempt to grab a new drawable, if any are still attached.
        // If yes, then focus that one. If it's null, then focus null instead to show the change.
        List<Node> nodes = tree.getChildNodeList();
        receiveEvent(new ActivationEvent(pluginInstance, nodes.size() > 0 ? nodes.get(0).getDrawable().getIdentifier() : null));
    }

    //
    // Painting
    //

    private @NotNull ModernTerminal getTerminal() {
        return session.getTerminal();
    }

    public @NotNull BasicTextGrid getBackBuffer() {
        return backBuffer;
    }

    private @NotNull BasicTextGrid getFrontBuffer() {
        return frontBuffer;
    }

    /**
     * Draws the contents of the requested {@link Drawable}, as well as any intersecting {@link Drawable}'s.
     *
     * @param rect the {@link Rect} to draw.
     */
    public void paint(@NotNull Rect rect) throws NodeNotFoundException, IOException {
        sessionWriteLock.lock();
        try {
            // Confirm WM status
            checkClosed();

            // Establish terminal boundaries
            final Size termSize = getTerminal().getSize();
            final Rect termBounds = new Rect(termSize);
            // Get the intersection of the rect of this container vs the rect of the drawable(s)
            final Rect targetRect = Rect.intersection(termBounds, rect);
            onTerminalResize(termSize);

            if (targetRect == null) {
                if (Main.DBG_RESPECT_TERMINAL_BOUNDS) {
                    throw new IllegalStateException(
                            "\n" + rect + "\n is completely outside the terminal boundaries \n" + termBounds);
                } else {
                    return;
                }
            }

            // Clear the target area in the buffer to prevent flickering
            for (int y = targetRect.getMinY(); y <= targetRect.getMaxY(); y++) {
                for (int x = targetRect.getMinX(); x <= targetRect.getMaxX(); x++) {
                    backBuffer.setCharacterAt(x, y, TextCharacter.DEFAULT);
                }
            }

            // Hide the cursor before painting to prevent flickering
            getTerminal().setCursorVisible(false);

            // Get drawable that intersect with the rectangle
            final List<Node> nodes = tree.getIntersectingChildNodes(targetRect);

            // Paint the canvas
            // Iterate in reverse
            ListIterator<Node> nodeIterator = nodes.listIterator(nodes.size());
            while (nodeIterator.hasPrevious()) {
                // Get drawable
                final Drawable drawable = nodeIterator.previous().getDrawable();

                // Get rectangles and intersect them
                final Rect drawableRect = drawable.getRelativeRect();
                final Rect targetIntersection = Rect.intersection(targetRect, drawableRect);
                if (targetIntersection == null) throw new IllegalStateException("THIS SHOULD NOT BE NULL");

                // Get canvas
                final TextGrid drawableTextGrid = drawable.getTextGrid();

                // Only paint to main canvas after validation
                if (drawableTextGrid.getSize().equals(drawableRect.getSize())) {
                    // Insert into backBuffer
                    drawableTextGrid.copyTo(
                            backBuffer,
                            targetIntersection.getMinY() - drawableRect.getMinY(),
                            targetIntersection.getHeight(),

                            targetIntersection.getMinX() - drawableRect.getMinX(),
                            targetIntersection.getWidth(),

                            targetIntersection.getMinY(), targetIntersection.getMinX()
                    );
                } else {
                    // Warn and add Drawable to list of bad Drawables
                    String warnMsg = drawable.toString() + "\n"
                            + drawableTextGrid + "\n"
                            + " tried to pass a BasicTextGrid with non-matching Size dimensions. The Drawable will be detached.";
                    throw new IllegalStateException(warnMsg);
                }
            }

            final Map<Point, TextCharacter> updateMap = new TreeMap<>(new Point.Comparator());

            // Map any cells to update
            for (int y = 0; y < termBounds.getHeight(); y++) {
                for (int x = 0; x < termBounds.getWidth(); x++) {
                    TextCharacter backBufferCharacter = getBackBuffer().getCharacterAt(x, y);
                    TextCharacter frontBufferCharacter = getFrontBuffer().getCharacterAt(x, y);

                    if (!backBufferCharacter.equals(frontBufferCharacter)) {
                        updateMap.put(new Point(x, y), backBufferCharacter);
                    }

                    // Multi-width characters
                    if (backBufferCharacter.isDoubleWidth()) {
                        // Skip the trailing padding
                        x++;
                    } else if (frontBufferCharacter.isDoubleWidth()) {
                        if (x + 1 < termBounds.getWidth()) {
                            updateMap.put(new Point(x + 1, y), frontBufferCharacter.withCharacter(' '));
                        }
                    }
                }
            }

            // If no updates are necessary
            if (updateMap.isEmpty()) return;

            // Set first values
            Point currentPosition = updateMap.keySet().iterator().next();
            getTerminal().setCursorPosition(currentPosition);

            TextCharacter firstScreenCharacterToUpdate = updateMap.values().iterator().next();
            EnumSet<TextEffect> currentTextEffect = firstScreenCharacterToUpdate.getModifiers();
            getTerminal().resetColorAndSGR();
            for (TextEffect textEffect : currentTextEffect) {
                getTerminal().enableTextEffect(textEffect);
            }

            TextColor currentForegroundColor = firstScreenCharacterToUpdate.getForegroundColor();
            TextColor currentBackgroundColor = firstScreenCharacterToUpdate.getBackgroundColor();
            getTerminal().setForegroundColor(currentForegroundColor);
            getTerminal().setBackgroundColor(currentBackgroundColor);

            // Go through update entries
            for (Point position : updateMap.keySet()) {
                if (!position.equals(currentPosition)) {
                    getTerminal().setCursorPosition(position.getX(), position.getY());
                    currentPosition = position;
                }

                TextCharacter newCharacter = updateMap.get(position);

                if (!currentForegroundColor.equals(newCharacter.getForegroundColor())) {
                    getTerminal().setForegroundColor(newCharacter.getForegroundColor());
                    currentForegroundColor = newCharacter.getForegroundColor();
                }
                if (!currentBackgroundColor.equals(newCharacter.getBackgroundColor())) {
                    getTerminal().setBackgroundColor(newCharacter.getBackgroundColor());
                    currentBackgroundColor = newCharacter.getBackgroundColor();
                }

                for (TextEffect textEffect : TextEffect.values()) {
                    if (currentTextEffect.contains(textEffect) && !newCharacter.getModifiers().contains(textEffect)) {
                        getTerminal().disableTextEffect(textEffect);
                        currentTextEffect.remove(textEffect);
                    } else if (!currentTextEffect.contains(textEffect) && newCharacter.getModifiers().contains(textEffect)) {
                        getTerminal().enableTextEffect(textEffect);
                        currentTextEffect.add(textEffect);
                    }
                }

                getTerminal().putString(newCharacter.getCharacter());

                if (newCharacter.isDoubleWidth()) {
                    // Double-width characters advances two columns
                    currentPosition = currentPosition.withRelativeX(2);
                } else {
                    // Normal characters advances one column
                    currentPosition = currentPosition.withRelativeX(1);
                }
            }

            // Update frontBuffer
            backBuffer.copyTo(frontBuffer);

            Node focusedNode = tree.getTreeFocusNode();
            if (focusedNode != null) {
                // The cursor has moved a lot during the drawing process,
                // so move it back to where it's supposed to be.
                getTerminal().setCursorPosition(focusedNode.getDrawable().getAbsoluteCursorRestingPoint());

                // Show cursor if wanted
                if (focusedNode.getDrawable().getCursorMode() == Drawable.CursorMode.TERMINAL_RENDERED) {
                    getTerminal().setCursorVisible(true);
                }
            }
        } finally {
            sessionWriteLock.unlock();
        }
    }

    //
    // Events
    //

    /**
     * Calls an {@link Event}, targeting it at a {@link Drawable} using its {@link EventChannel}.
     * If the {@link Drawable} is a {@link DrawableContainer}, then its children will be targeted as well,
     * gradually stepping up the tree until it meets end nodes.
     *
     * @param event    The event to call.
     * @param drawable The drawable to target.
     */
    public void callEvent(@NotNull Event event, @NotNull Drawable drawable) {
        // A list of drawable to send the events to
        List<Drawable> drawableList = new ArrayList<>();
        drawableList.add(drawable);

        while (drawableList.size() > 0) {
            Drawable subDrawable = drawableList.remove(0);

            // Send the event to the drawable
            EventChannel channel = subDrawable.getChannel();
            AccelaAPI.getPluginManager().callEvent(event, channel);

            // Check if it contains more drawables. If yes, then add those to the list
            if (subDrawable instanceof DrawableContainer) {
                try {
                    // Add (immediate) child drawables to the list
                    Branch branch = ((DrawableContainer) subDrawable).getBranch();
                    drawableList.addAll(branch.getChildDrawables());
                } catch (NodeNotFoundException ex) {
                    session.getLogger().log(Level.WARNING, "Node not found", ex);
                }
            }
        }
    }

    /**
     * Broadcasts an {@link Event} to all {@link Drawable}s,
     * without any additional parsing.
     *
     * @param event The event to broadcast
     */
    private void performEventBroadcast(@NotNull Event event) {
        broadcastLock.lock();
        try {
            pluginInstance.getLogger().log(Level.INFO, "Performing broadcast ..." + event);

            List<Node> childNodes = tree.getTreeNodeList();
            for (Node node : childNodes) {
                AccelaAPI.getPluginManager().callEvent(event, node.drawable.getChannel());
            }
        } finally {
            broadcastLock.unlock();
        }
    }

    /**
     * Broadcasts an {@link Event} to all {@link Drawable}s
     *
     * @param event The event to broadcast
     */
    private void receiveEvent(final @NotNull Event event) {
        // Focus mods etc
        /*
        // todo make shortcuts customizable
        if (event instanceof SpecialInputEvent) {
            final SpecialInputEvent specialInputEvent = (SpecialInputEvent) event;

            if (specialInputEvent.getKey() == SpecialInputEvent.SpecialKey.HT) {
                Node focusedNode = tree.getTreeFocusNode();
                if (focusedNode == null) {
                    List<Node> nodes = tree.getChildNodeList();

                    int index = nodes.indexOf(DrawableTree.getNode(focusedNode.getDrawable()));
                    if (index + 1 > nodes.size()) index = 0;
                    Drawable drawable = nodes.get(index).getDrawable();
                    performEventBroadcast(new ActivationEvent(pluginInstance, drawable.getIdentifier()));
                }
            }
        }
         */

        // Send the event so that it can be parsed "raw" if need be.
        performEventBroadcast(event);
    }

    private void checkClosed() {
        if (isClosed) throw new IllegalStateException("Interaction with dead WM");
    }

    public void close() {
        isClosed = true;

        // Detach all drawable
        tree.killNodes();

        // Unregister events
        AccelaAPI.getPluginManager().unregisterEvents(broadcastListener);
    }

    public void writeToSession(byte... by) throws IOException {
        sessionWriteLock.lock();
        try {
            getTerminal().putString(new String(by, AbstractTerminal.UTF8_CHARSET));
        } finally {
            sessionWriteLock.unlock();
        }
    }

    public void writeToSession(String string) throws IOException {
        sessionWriteLock.lock();
        try {
            getTerminal().putString(string);
        } finally {
            sessionWriteLock.unlock();
        }
    }

    public void writeToSession(CharSequence string) throws IOException {
        sessionWriteLock.lock();
        try {
            getTerminal().putString((String) string);
        } finally {
            sessionWriteLock.unlock();
        }
    }

    //
    // Event listeners
    //

    /**
     * Listens for {@link Prismatic} events related to the local session.
     */
    class BroadcastListener implements Listener {
        @EventHandler
        public void onActivationEvent(ActivationEvent event) {
            receiveEvent(event);
        }

        @EventHandler
        public void onInputEvent(KeyStroke event) {
            receiveEvent(event);
        }
    }

    /**
     * Listens for terminal events firing in the local sessions channel
     */
    class SessionListener implements Listener {
        @EventHandler
        public void onTerminalResizeEvent(TerminalResizeEvent event) {
            onTerminalResize(event.getSize());
        }
    }

    private synchronized void onTerminalResize(Size size) {
        try {
            sessionWriteLock.lock();

            if (!previousTerminalSize.equals(size)) {
                previousTerminalSize = size;
                backBuffer.resize(size, TextCharacter.DEFAULT);
                frontBuffer.resize(size, TextCharacter.DEFAULT);

                getTerminal().resetColorAndSGR();
                getTerminal().clear();
                backBuffer.setAllCharacters(TextCharacter.DEFAULT);
                frontBuffer.setAllCharacters(TextCharacter.DEFAULT);
                paint(new Rect(size));
            }
        } catch (IOException e) {
            session.getLogger().log(Level.WARNING, "Exception when clearing terminal after resize event", e);
        } finally {
            sessionWriteLock.unlock();
        }
    }
}
