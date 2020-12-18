package net.accela.prisma;

import net.accela.ansi.AnsiLib;
import net.accela.ansi.sequence.CSISequence;
import net.accela.ansi.sequence.ESCSequence;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.event.*;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Point;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import net.accela.prisma.session.TextGraphicsSession;
import net.accela.prisma.util.Canvas;
import net.accela.prisma.util.SequencePainter;
import net.accela.prisma.util.drawabletree.Branch;
import net.accela.prisma.util.drawabletree.DrawableTree;
import net.accela.prisma.util.drawabletree.Node;
import net.accela.server.AccelaAPI;
import net.accela.server.event.Event;
import net.accela.server.event.EventChannel;
import net.accela.server.event.EventHandler;
import net.accela.server.event.Listener;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * A TUI window manager
 */
public class PrismaWM implements Container {
    // The session hosting this
    final TextGraphicsSession session;

    // The listener for this session
    // Don't forget to close this.
    final BroadcastListener broadcastListener = new BroadcastListener();

    // The plugin instance for WindowManager
    protected final Plugin thisPlugin;

    // Flags
    // Whether the WindowManager is alive or not
    boolean isAlive = true;

    // Mouse related
    // TODO: 10/23/20 move mouse handling to textGraphicsSession
    public enum MouseMode {
        NONE,
        ONLY_BUTTON_PRESS,
        NORMAL,
        BUTTON,
        ANY
    }

    // The current mode
    @NotNull MouseMode mouseMode = MouseMode.NONE;

    // Drawables
    // The Drawable objects attached to this
    final DrawableTree tree = new DrawableTree(this);

    // Event
    public final EventChannel broadcast = new EventChannel();

    // Locks
    final Lock paintLock = new ReentrantLock();
    final Lock broadcastLock = new ReentrantLock();

    public PrismaWM(@NotNull TextGraphicsSession session) {
        this.session = session;

        // Get plugin instance
        Plugin instance = Main.getInstance();
        if (instance == null) throw new IllegalStateException("Missing plugin instance");
        thisPlugin = instance;

        // Reset the terminal to its initial state and clear it
        writeToSession(ESCSequence.RIS_STRING + CSISequence.CLR_STRING);

        // Register event listeners
        AccelaAPI.getPluginManager().registerEvents(broadcastListener, thisPlugin, broadcast);

        // Enable mouse
        setMouseMode(PrismaWM.MouseMode.NORMAL);
        setMouseMode(PrismaWM.MouseMode.ANY);
    }

    public @NotNull EventChannel getBroadcastChannel() {
        return broadcast;
    }

    public void checkClosed() {
        if (!isAlive) throw new IllegalStateException("Interaction with dead WM");
    }

    /**
     * @return The session this window manager is catering for.
     */
    public @NotNull TextGraphicsSession getSession() {
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attach(@NotNull Drawable drawable, @NotNull Plugin plugin)
            throws RectOutOfBoundsException, NodeNotFoundException {
        // Checks
        checkClosed();
        if (!Rect.fits(getRelativeRect(), drawable.getRelativeRect())) {
            throw new RectOutOfBoundsException("Drawable does not fit within the terminal");
        }

        // Attach
        tree.newNode(drawable, plugin);

        // Register any events
        AccelaAPI.getPluginManager().registerEvents(drawable, drawable.getPlugin(), drawable.getChannel());

        // Focus
        //fixme broadcastEvent(new ActivationEvent(thisPlugin, drawable.identifier));

        /*
        // Show/Hide cursor
        //fixme also move cursor
        if(drawable.cursorEnabled()) writeToSession(AnsiLib.showCursor);
        else writeToSession(AnsiLib.hideCursor);
         */
        thisPlugin.getLogger().log(Level.INFO, "Attached drawable '" + drawable + "'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detach(@NotNull Drawable drawable) throws NodeNotFoundException {
        thisPlugin.getLogger().log(Level.INFO, "Detaching drawable '" + drawable + "'");

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
        broadcastEvent(new ActivationEvent(thisPlugin, nodes.size() > 0 ? nodes.get(0).getDrawable().identifier : null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Rect getRelativeRect() {
        return new Rect(session.getTerminalSize());
    }

    /**
     * Synonymous to {@link PrismaWM#getRelativeRect()}.
     */
    @Override
    public @NotNull Rect getAbsoluteRect() {
        return getRelativeRect();
    }

    //
    // Painting
    //

    @Override
    public void paint(@NotNull Rect initialTargetRect) throws NodeNotFoundException {
        paintLock.lock();
        try {
            // Confirm WM status
            checkClosed();

            // Get the intersection of the rect of this container vs the rect of the drawable(s)
            final Rect termBounds = getRelativeRect();
            final Rect targetRect = Rect.intersection(termBounds, initialTargetRect);
            if (targetRect == null) throw new IllegalStateException(
                    "\n" + initialTargetRect + "\n is outside the terminal boundaries \n" + termBounds);

            // Hide the cursor before painting to prevent flickering
            writeToSession(CSISequence.P_CUR_OFF);

            // Create a new canvas
            Canvas canvas = new Canvas(targetRect.getSize());

            // Get drawables that intersect with the rectangle
            final List<Drawable> drawables = getIntersectingImmediateDrawables(targetRect);

            // Paint the canvas
            // Iterate in reverse
            ListIterator<Drawable> drawableIterator = drawables.listIterator(drawables.size());
            while (drawableIterator.hasPrevious()) {
                // Get drawable
                final Drawable drawable = drawableIterator.previous();

                // Get rectangles and intersect them
                final Rect drawableRect = drawable.getRelativeRect();
                final Rect targetIntersection = Rect.intersection(targetRect, drawableRect);
                if (targetIntersection == null) throw new IllegalStateException("THIS SHOULD NOT BE NULL");

                // Get canvas
                final Canvas drawableCanvas = drawable.getCanvas();

                // Only paint to main canvas after validation
                if (drawableCanvas.getSize().equals(drawableRect.getSize())) {
                    // Paint the Canvas
                    Canvas.paintHard(canvas, targetRect.getStartPoint(), drawableCanvas, drawableRect.getStartPoint());
                } else {
                    // Warn and add Drawable to list of bad Drawables
                    String warnMsg = drawable.toString() + "\n"
                            + drawableCanvas + "\n"
                            + " tried to pass a Canvas with non-matching Size dimensions. The Drawable will be detached.";
                    throw new IllegalStateException(warnMsg);
                }
            }

            // Paint terminal from canvas
            SequencePainter sequencePainter = new SequencePainter();
            for (int y = targetRect.getMinY(); y < targetRect.getMaxY() + 1; y++) {
                // Move the cursor into position
                writeToSession(AnsiLib.CUP(targetRect.getMinX() + 1, y + 1));

                for (int x = targetRect.getMinX(); x < targetRect.getMaxX() + 1; x++) {
                    // Get the cell we're at
                    final Canvas.Cell cell = canvas.get(x - targetRect.getMinX(), y - targetRect.getMinY());

                    // Reset painting attributes to prevent sequence bleed
                    // todo replace with a smart system
                    writeToSession(SGRSequence.RESET);

                    if (cell == null) {
                        /*
                        if(sequencePainter.getCurrentStatements().size() > 0){
                            sequencePainter.reset();
                            writeToSession(SGRSequence.RESET);
                        }
                         */
                        writeToSession(" ");
                    } else {
                        // Update current SGR statements
                        // sequence logic
                        // fixme no sequence logic for now, using dumb graphics to lessen the risk
                        //  of bugs during first tests...
                        SGRSequence sequence = cell.getSequence();
                        if (sequence != null) writeToSession(sequence);

                        /*
                        SGRSequence sequence = cell.getSequence();
                        if(sequence != null){
                            // Add new statements
                            sequencePainter.eliminate(cell.getSequence().toSGRStatements());

                            List<SGRStatement> currentStatements = sequencePainter.getCurrentStatements();
                            if(currentStatements.size() > 0){

                            } else {
                                if(sequencePainter.getCurrentStatements().size() > 0){
                                    sequencePainter.reset();
                                    writeToSession(SGRSequence.RESET);
                                }
                            }
                        } else {
                            if(sequencePainter.getCurrentStatements().size() > 0){
                                sequencePainter.reset();
                                writeToSession(SGRSequence.RESET);
                            }
                        }
                         */

                        // code point logic
                        String codePoint = cell.getCodepoint();
                        if (codePoint != null) {
                            switch (cell.getCharacterWidth()) {
                                case -1:
                                    // todo plugin blaming system
                                    session.getLogger().log(Level.WARNING, "'" + codePoint
                                            + "' - C0/C1 control characters are not allowed");
                                    writeToSession(" ");
                                    break;
                                case 0:
                                    writeToSession(codePoint + " ");
                                case 1:
                                    writeToSession(codePoint);
                                    break;
                                case 2:
                                    writeToSession(codePoint);
                                    x++;
                                    break;
                            }
                        } else {
                            writeToSession(" ");
                        }
                    }
                }
            }

            Node focusedNode = tree.getTreeFocusNode();
            if (focusedNode != null) {
                // The cursor has moved a lot during the drawing process,
                // so move it back to where it's supposed to be.
                moveTextCursor(focusedNode.getDrawable().getCursorRestingPoint());

                // Show cursor if wanted
                if (focusedNode.getDrawable().cursorEnabled()) writeToSession(CSISequence.P_CUR_ON);
            }
        } finally {
            paintLock.unlock();
        }
    }

    private List<@NotNull Drawable> getIntersectingImmediateDrawables(@NotNull Rect rect) {
        final List<Drawable> drawables = new ArrayList<>();
        for (Node node : tree.getChildNodeList()) {
            Drawable drawable = node.getDrawable();
            if (rect.intersects(drawable.getRelativeRect())) {
                drawables.add(drawable);
            }
        }
        return drawables;
    }

    //
    // TEXT CURSOR
    //

    /**
     * Moves the terminal cursor used for text editing.
     * Values start at 0, since that's what the rest of this system uses.
     * It does the conversion to ANSIs 1 based coordinate system on its own.
     * <p>
     * //@param caller The caller, which has to be focused.
     *
     * @param point Minimum 0
     */
    public void moveTextCursor(@NotNull Point point) {
        //if(areaMapper.getTopDrawable() != caller) return;
        writeToSession(AnsiLib.CUP(point.getX() + 1, point.getY() + 1));
    }

    //
    // MOUSE RELATED
    //

    void setMouseMode(@NotNull MouseMode mode) {
        //if(!doorwayModeEnabled) enableDoorwayMode();
        switch (mode) {
            case NONE:
                writeToSession(CSISequence.CSI_STRING + "?1000l");
                break;
            case ONLY_BUTTON_PRESS:
                writeToSession(CSISequence.CSI_STRING + "?9h");
                break;
            case NORMAL:
                writeToSession(CSISequence.CSI_STRING + "?1000h");
                break;
            case BUTTON:
                writeToSession(CSISequence.CSI_STRING + "?1002h");
                break;
            case ANY:
                writeToSession(CSISequence.CSI_STRING + "?1003h");
                break;
        }
        System.out.println("changed mouse mode");
        mouseMode = mode;
    }

    //
    // I/O
    //

    /**
     * Properly shuts down the WindowManager, closes streams, etc.
     * What this method actually does depends heavily on the implementation,
     * and thus it should not be relied upon. It is simply intended to make
     * the shutdown process a little less jarring.
     */
    public void close() {
        checkClosed();
        isAlive = false;

        // Detach all drawables
        tree.killNodes();

        // Unregister events
        AccelaAPI.getPluginManager().unregisterEvents(broadcastListener);
    }

    void writeToSession(@NotNull CharSequence characters) {
        checkClosed();
        session.writeToClient(characters.toString());
    }

    //
    // EVENT RELATED
    //

    /**
     * Calls an {@link Event}, targeting it at a {@link Drawable} using its {@link EventChannel}.
     * If the {@link Drawable} is a {@link DrawableContainer}, then its children will be targeted as well,
     * gradually stepping up the tree until it meets end nodes.
     *
     * @param event    The event to call.
     * @param drawable The drawable to target.
     */
    public void callEvent(@NotNull WMEvent event, @NotNull Drawable drawable) {
        // A list of drawables to send the events to
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
    void performBroadcast(@NotNull WMEvent event) {
        thisPlugin.getLogger().log(Level.INFO, "Performing broadcast ..." + event);

        List<Node> childNodes = tree.getTreeNodeList();
        for (Node node : childNodes) {
            AccelaAPI.getPluginManager().callEvent(event, node.drawable.getChannel());
        }
    }

    /**
     * Broadcasts an {@link Event} to all {@link Drawable}s
     *
     * @param event The event to broadcast
     */
    public void broadcastEvent(final @NotNull WMEvent event) {
        broadcastLock.lock();
        try {
            // Focus mods etc
            // todo make shortcuts customizable
            if (event instanceof SpecialInputEvent) {
                SpecialInputEvent specialInputEvent = (SpecialInputEvent) event;

                if (specialInputEvent.getKey() == SpecialInputEvent.SpecialKey.HT) {
                    Node focusedNode = tree.getTreeFocusNode();
                    if (focusedNode != null) {
                        List<Node> nodes = tree.getChildNodeList();

                        int index = nodes.indexOf(DrawableTree.getNode(focusedNode.getDrawable()));
                        if (index + 1 > nodes.size()) index = 0;
                        Drawable drawable = nodes.get(index).getDrawable();
                        performBroadcast(new ActivationEvent(thisPlugin, drawable.identifier));
                    }
                }
            }
            // FIXME: 11/24/20 drawable overlap handling
            else if (event instanceof MouseInputEvent) {
                MouseInputEvent mouseInputEvent = (MouseInputEvent) event;

                // Get Drawables that intersect with the point
                Rect pointRect = new Rect(mouseInputEvent.getPoint());
                List<Node> intersectingChildNodes = tree.getIntersectingNodes(pointRect);

                Node focusNode = null;
                int index = 0;
                while (intersectingChildNodes.size() > index) {
                    focusNode = intersectingChildNodes.get(index);

                    if (!focusNode.drawable.wantsFocus()) {
                        index++;
                        System.out.println("drawable '" + focusNode.drawable + "' does not want focus, skipping");
                        continue;
                    }

                    if (focusNode instanceof Branch) {
                        Branch branch = (Branch) focusNode;

                        Point startPoint = focusNode.drawable.getRelativeRect().getStartPoint();
                        pointRect = Rect.startPointSubtraction(pointRect, startPoint);

                        intersectingChildNodes = branch.getIntersectingNodes(pointRect);
                        index = 0;
                    } else {
                        break;
                    }
                }

                if (focusNode != null) {
                    performBroadcast(new ActivationEvent(thisPlugin, focusNode.drawable.identifier));
                }
            }

            // Send the event so that it can be parsed "raw" if need be.
            performBroadcast(event);
        } finally {
            broadcastLock.unlock();
        }
    }

    // Listener
    class BroadcastListener implements Listener {
        @EventHandler
        public void onActivationEvent(ActivationEvent event) {
            broadcastEvent(event);
        }

        @EventHandler
        public void onMouseInputEvent(MouseInputEvent event) {
            broadcastEvent(event);
        }

        @EventHandler
        public void onPointInputEvent(PointInputEvent event) {
            broadcastEvent(event);
        }

        @EventHandler
        public void onSpecialInputEvent(SpecialInputEvent event) {
            broadcastEvent(event);
        }

        @EventHandler
        public void onStringInputEvent(StringInputEvent event) {
            broadcastEvent(event);
        }
    }
}
