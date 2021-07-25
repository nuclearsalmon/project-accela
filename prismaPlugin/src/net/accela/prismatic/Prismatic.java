package net.accela.prismatic;

import net.accela.prismatic.behaviour.MouseCaptureMode;
import net.accela.prismatic.event.FocusEvent;
import net.accela.prismatic.event.TerminalResizeEvent;
import net.accela.prismatic.input.lanterna.actions.InputEvent;
import net.accela.prismatic.session.TextGraphicsSession;
import net.accela.prismatic.terminal.AbstractTerminal;
import net.accela.prismatic.terminal.ModernTerminal;
import net.accela.prismatic.ui.geometry.Point;
import net.accela.prismatic.ui.geometry.Rect;
import net.accela.prismatic.ui.geometry.Size;
import net.accela.prismatic.ui.geometry.exception.RectOutOfBoundsException;
import net.accela.prismatic.ui.text.BasicTextGrid;
import net.accela.prismatic.ui.text.TextCharacter;
import net.accela.prismatic.ui.text.TextGrid;
import net.accela.prismatic.ui.text.color.TextColor;
import net.accela.prismatic.ui.text.effect.TextEffect;
import net.accela.server.AccelaAPI;
import net.accela.server.event.Event;
import net.accela.server.event.EventChannel;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class Prismatic implements ContainerInterface, Closeable {
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


    final BroadcastListener broadcastListener = new BroadcastListener();

    MouseCaptureMode mouseCaptureMode = null;

    final Lock sessionWriteLock = new ReentrantLock();
    final Lock broadcastLock = new ReentrantLock();

    final BasicTextGrid backBuffer;
    final BasicTextGrid frontBuffer;

    private Size previousTerminalSize;

    protected final List<@NotNull Drawable> childDrawables = new LinkedList<>();
    protected @Nullable DrawableIdentifier focusTarget;

    /**
     * Whether new drawables are inserted on top
     */
    protected boolean insertNewDrawablesOnTop = true;

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
                    InputEvent inputEvent = null;
                    try {
                        inputEvent = getTerminal().pollInput();
                        if (inputEvent == null) continue;
                        receiveEvent(inputEvent);
                    } catch (IOException ex) {
                        closeReason = "IOException";
                        ex.printStackTrace();
                        break;
                    }
                }
                try {
                    close();
                } catch (IOException e) {
                    pluginInstance.getLogger().log(Level.SEVERE, "Failed to close Prismatic", e);
                }
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
    // ContainerInterface methods
    //

    /**
     * Attaches a {@link Drawable} to this {@link ContainerInterface}
     *
     * @param drawable The {@link Drawable} to attach
     * @throws RectOutOfBoundsException If the rect is out of bounds or otherwise invalid
     */
    public synchronized void attach(final @NotNull Drawable drawable) throws RectOutOfBoundsException, IOException {
        synchronized (this) {
            // Confirm state
            checkClosed();

            // Confirm attachment
            if (childDrawables.contains(drawable)) {
                throw new IllegalArgumentException("Drawable is already attached to this container");
            }

            if (Main.DBG_RESPECT_TERMINAL_BOUNDS
                    && !Rect.contains(new Rect(getTerminal().getSize()), drawable.getRelativeRect())) {
                throw new RectOutOfBoundsException("Drawable does not fit within the terminal");
            }

            // Attach
            synchronized (childDrawables) {
                childDrawables.add(getInsertionIndex(), drawable);
            }
            drawable.attachSelf(this);

            // Register any events
            registerDrawableEvents(drawable);

            // Focus
            if (Main.DBG_FOCUS_ON_WM_ATTACHMENT) {
                setFocusedDrawable(drawable);
            }
        }
    }

    protected int getInsertionIndex() {
        if (insertNewDrawablesOnTop) {
            return childDrawables.size();
        } else {
            return 0;
        }
    }

    void detachAll(@NotNull Drawable... drawables) throws IOException {
        detachAll(true, drawables);
    }

    void detachAll(boolean repaint, final @NotNull Drawable... drawables) throws IOException {
        for (Drawable drawable : drawables) {
            detach(drawable, repaint);
        }
    }

    @Override
    public synchronized void detach(@NotNull Drawable drawable) throws IOException {
        detach(drawable, true);
    }

    public synchronized void detach(@NotNull Drawable drawable, boolean repaint) throws IOException {
        synchronized (this) {
            // Confirm attachment
            if (!childDrawables.contains(drawable)) {
                throw new IllegalArgumentException("Drawable is not attached to this container");
            }

            // Get the rect before detaching, we're probably going to need it later
            Rect rect = drawable.getAbsoluteRect();

            // Detach
            synchronized (childDrawables) {
                childDrawables.remove(drawable);
            }
            drawable.attachSelf(null);

            // Unregister events
            unregisterDrawableEvents(drawable);

            if (repaint) {
                // Re-focus if needed
                if (focusTarget != null && focusTarget.getDrawable() == drawable) {
                    Drawable newFocusedDrawable = childDrawables.size() > 0 ? childDrawables.get(0) : null;
                    setFocusedDrawable(newFocusedDrawable);
                }

                // Redraw the now empty rect
                render(rect);
            }
        }
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
    public void render(@NotNull Rect rect) throws IOException {
        sessionWriteLock.lock();
        try {
            // Confirm WM status
            checkClosed();

            // Establish terminal boundaries
            final Size termSize = getTerminal().getSize();
            final Rect termBounds = new Rect(termSize);
            // Update buffers if resized. Not doing this before drawing will result in exceptions.
            onTerminalResize(termSize);

            // Get the intersection of the rect of this container vs the requested rect
            final Rect targetRect = Rect.intersection(termBounds, rect);

            // Check out of bounds behaviour
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

            // Get drawable that intersect with the rectangle
            final List<Drawable> intersectingDrawables = getIntersectingDrawables(targetRect);

            // Paint the canvas
            // Iterate in reverse
            ListIterator<Drawable> drawableIterator = intersectingDrawables.listIterator(intersectingDrawables.size());
            while (drawableIterator.hasPrevious()) {
                // Get drawable
                final Drawable drawable = drawableIterator.previous();

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
                    String warnMsg = "\n"
                            + drawable.toString() + "\n"
                            + "vs \n"
                            + drawableTextGrid.getSize() + ".\n"
                            + "Tried to pass a Drawable with non-matching Size dimensions. The Drawable will be detached.";
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

            // Hide the cursor before painting to prevent flickering
            getTerminal().setCursorVisible(false);

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

            // Focus actions
            if (focusTarget != null) {
                final Drawable focusedDrawable = focusTarget.getDrawable();
                // The cursor has moved a lot during the drawing process,
                // so move it back to where it's supposed to be.
                getTerminal().setCursorPosition(focusedDrawable.getAbsoluteCursorRestingPoint());

                // Show cursor if wanted
                if (focusedDrawable.getCursorMode() == CursorMode.TERMINAL_RENDERED) {
                    getTerminal().setCursorVisible(true);
                }
            }
        } finally {
            sessionWriteLock.unlock();
        }
    }

    //
    // Calculations
    //

    /**
     * @param rect The {@link Rect} to look for {@link Drawable}s within. Relative.
     * @return All {@link Drawable}s that are situated within the {@link Rect} provided
     */
    public @NotNull List<@NotNull Drawable> getIntersectingDrawables(@NotNull Rect rect) {
        synchronized (this.childDrawables) {
            List<Drawable> drawables = new ArrayList<>();
            for (Drawable drawable : this.childDrawables) {
                if (rect.intersects(drawable.getRelativeRect())) {
                    drawables.add(drawable);
                }
            }
            return drawables;
        }
    }

    //
    // Positioning
    //

    @Override
    public int getDepth(@NotNull Drawable drawable) {
        return childDrawables.indexOf(drawable);
    }

    /**
     * @param relDepth The relative depth.
     */
    public void setRelativeDepth(int relDepth, @NotNull Drawable drawable) {
        synchronized (childDrawables) {
            setAbsoluteDepth(childDrawables.size() - relDepth, drawable);
        }
    }

    /**
     * @param absDepth The absolute depth.
     */
    public void setAbsoluteDepth(int absDepth, @NotNull Drawable drawable) {
        synchronized (childDrawables) {
            if (!childDrawables.contains(drawable)) {
                throw new IllegalArgumentException("Drawable not attached to this Container!");
            }

            // Adjust to be within limits
            absDepth = Math.max(Math.min(absDepth, childDrawables.size()), 0);

            // Re-insert at desired depth
            childDrawables.remove(drawable);
            childDrawables.add(absDepth, drawable);
        }
    }

    @Override
    public void setFocusedDrawable(@Nullable Drawable drawable) {
        if (drawable == null) {
            focusTarget = null;
            broadcastEvent(new FocusEvent(pluginInstance, null));
        } else {
            if (!childDrawables.contains(drawable)) {
                throw new IllegalArgumentException("Drawable not attached to this Container!");
            }

            // Move
            synchronized (childDrawables) {
                childDrawables.remove(drawable);
                childDrawables.add(0, drawable);
            }

            // Push focus event
            broadcastEvent(new FocusEvent(pluginInstance, drawable.identifier));

            // Terminal cursor
            boolean showTermCursor = drawable.getCursorMode() == CursorMode.TERMINAL_RENDERED;
            try {
                // Show or hide terminal cursor
                getTerminal().setCursorVisible(showTermCursor);
            } catch (IOException e) {
                session.getLogger().log(Level.WARNING, "Failed to make terminal cursor visible on Drawable focus", e);
            }
            if (showTermCursor) {
                // Move terminal cursor
                try {
                    getTerminal().setCursorPosition(drawable.getAbsoluteCursorRestingPoint());
                } catch (IOException e) {
                    session.getLogger().log(Level.WARNING, "Failed to move cursor on Drawable focus", e);
                }
            }
        }
    }

    //
    // Events and I/O
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
        final List<Drawable> drawableList = new ArrayList<>();

        // Populate list
        drawableList.add(drawable);
        if (drawable instanceof DrawableContainer) {
            final DrawableContainer drawableContainer = (DrawableContainer) drawable;
            drawableList.addAll(drawableContainer.getAllChildDrawables());
        }

        // Send events
        for (Drawable subDrawable : drawableList) {
            EventChannel channel = subDrawable.getChannel();
            AccelaAPI.getPluginManager().callEvent(event, channel);
        }
    }

    /**
     * Broadcasts an {@link Event} to all {@link Drawable}s,
     * without any additional parsing.
     *
     * @param event The event to broadcast
     */
    private void broadcastEvent(@NotNull Event event) {
        broadcastLock.lock();
        try {
            pluginInstance.getLogger().log(Level.INFO, "Performing broadcast ..." + event);

            for (Drawable drawable : childDrawables) {
                callEvent(event, drawable);
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
                    performEventBroadcast(new FocusEvent(pluginInstance, drawable.getIdentifier()));
                }
            }
        }
         */

        // Send the event so that it can be parsed "raw" if need be.
        broadcastEvent(event);
    }

    private void checkClosed() {
        if (isClosed) throw new IllegalStateException("Interaction with dead WM");
    }

    public void close() throws IOException {
        // Detach all drawables
        detachAll(false, childDrawables.toArray(new Drawable[0]));

        // Unregister events
        AccelaAPI.getPluginManager().unregisterEvents(broadcastListener);

        isClosed = true;
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
        public void onActivationEvent(FocusEvent event) {
            receiveEvent(event);
        }

        @EventHandler
        public void onInputEvent(InputEvent event) {
            receiveEvent(event);
        }

        @EventHandler
        public void onFocus(FocusEvent event) {
            focusTarget = event.getTarget();
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
                render(new Rect(size));
            }
        } catch (IOException e) {
            session.getLogger().log(Level.WARNING, "Exception when clearing terminal after resize event", e);
        } finally {
            sessionWriteLock.unlock();
        }
    }
}
