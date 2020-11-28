package net.accela.prisma;

import net.accela.ansi.AnsiLib;
import net.accela.ansi.sequence.SGRSequence;
import net.accela.prisma.event.*;
import net.accela.prisma.exception.DeadWMException;
import net.accela.prisma.exception.MissingPluginInstanceException;
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
import java.util.logging.Level;

/**
 * A TUI window manager
 */
@SuppressWarnings("deprecation")
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
    // Whether doorway mode is enabled or not
    boolean doorwayModeEnabled = false;

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
    @NotNull Point mousePoint = new Point(0, 0);

    // Drawables
    // The Drawable objects attached to this
    final DrawableTree tree = new DrawableTree(this);

    // Event
    public final EventChannel broadcast = new EventChannel();

    public PrismaWM(@NotNull TextGraphicsSession session) throws MissingPluginInstanceException {
        this.session = session;

        // Get plugin instance
        Plugin instance = Main.getInstance();
        if (instance == null) throw new MissingPluginInstanceException();
        thisPlugin = instance;

        // Reset the terminal to its initial state and clear it
        writeToSession(AnsiLib.RIS + AnsiLib.CLR);

        // Register event listeners
        AccelaAPI.getPluginManager().registerEvents(broadcastListener, thisPlugin, broadcast);

        // Enable mouse
        enableDoorwayMode();
        setMouseMode(PrismaWM.MouseMode.NORMAL);
        setMouseMode(PrismaWM.MouseMode.ANY);
    }

    public @NotNull EventChannel getBroadcastChannel() {
        return broadcast;
    }

    public void checkClosed() throws DeadWMException {
        if (!isAlive) throw new DeadWMException();
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
    public synchronized void attach(@NotNull Drawable drawable, @NotNull Plugin plugin)
            throws RectOutOfBoundsException, DeadWMException, NodeNotFoundException {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void detach(@NotNull Drawable drawable) throws NodeNotFoundException {
        // Get the rect before detaching, we're going to need it later
        Rect rect = drawable.getAbsoluteRect();

        // Detach
        Node node = DrawableTree.getNode(drawable);
        if (node != null) node.kill();

        // Redraw the now empty rect
        paint(rect);

        // Attempt to grab a new drawable, if any are still attached.
        // If yes, then focus that one. If it's null, then focus null instead to show the change.
        List<Node> nodes = tree.getChildNodes();
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

    @Override
    public void tryRect(@NotNull Drawable drawable, @NotNull Rect rect) throws RectOutOfBoundsException {
        if (!Rect.fits(getRelativeRect(), drawable.getRelativeRect())) {
            throw new RectOutOfBoundsException("Drawable does not fit within the container");
        }
    }

    //
    // Painting
    //

    @Override
    public synchronized void paint(@NotNull Rect initialTargetRect) throws NodeNotFoundException {
        // Confirm WM status
        checkClosed();

        // Get the intersection of the rect of this container vs the rect of the drawable(s)
        final Rect targetRect = Rect.intersection(getRelativeRect(), initialTargetRect);
        if (targetRect == null) throw new IllegalStateException("The Drawable is outside the screen boundaries");

        // Hide the cursor before painting to prevent flickering
        writeToSession(AnsiLib.hideCursor);

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
                //session.getLogger().log(Level.WARNING, warnMsg);
                //drawable.getPlugin().getLogger().log(Level.WARNING, warnMsg);
                // fixme absolute shit tier pajeet code, don't do this you dumbass
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

        // The cursor has moved a lot during the drawing process,
        // so move it back to where it's supposed to be.
        Node focusedNode = tree.getFocusedNode();
        if (focusedNode != null) {
            moveTextCursor(focusedNode.getDrawable().getCursorRestingPoint());
            // Show cursor if wanted
            if (focusedNode.getDrawable().cursorEnabled()) writeToSession(AnsiLib.showCursor);
        }

        // Show cursor
        writeToSession(AnsiLib.showCursor);
    }

    /*
    /**
     * {@inheritDoc}
     */
    /*
    public synchronized void draw(@NotNull Rect targetRect) throws DeadWMException, NodeNotFoundException {
        checkClosed();

        writeToSession(AnsiLib.hideCursor);

        // Get the intersection of the rect of this container vs the target
        final Rect targetRectIntersection = Rect.intersection(getRelativeRect(), targetRect);

        // Get all drawables within the rect
        final List<@NotNull Drawable> drawables = tree.getIntersectingDrawables(targetRectIntersection);

        thisPlugin.getLogger().log(
                Level.INFO, "Drawables within " + targetRectIntersection + ":" + Arrays.toString(drawables.toArray()));

        // A grid for memorizing which areas are occupied and which are not
        final MutableGrid<@NotNull Boolean> occupiedGrid = new MutableGrid<>(targetRectIntersection.getSize());
        occupiedGrid.fill(Boolean.FALSE);

        // Mark which parts of the grid are occupied
        for (Drawable drawable:drawables) {
            Rect relativeDrawableIntersection =
                    Rect.intersection(targetRectIntersection, drawable.getRelativeRect()).startPointSubtraction(targetRectIntersection);
            for (int y = relativeDrawableIntersection.getMinY(); y <= relativeDrawableIntersection.getMaxY(); y++) {
                for (int x = relativeDrawableIntersection.getMinX(); x <= relativeDrawableIntersection.getMaxX(); x++) {
                    occupiedGrid.set(x, y, true);
                }
            }
        }

        // Clear any previous effects
        writeToSession(new SGRSequence(new SGRStatement(SGRStatement.Type.RESET)));

        // Draw over any blank space not occupied by drawables
        for (int y = targetRectIntersection.getMinY(); y <= targetRectIntersection.getMaxY(); y++) {
            boolean needMove = true;
            for (int x = targetRectIntersection.getMinX(); x <= targetRectIntersection.getMaxX(); x++) {
                // Move the cursor into position
                if(needMove){
                    writeToSession(AnsiLib.CUP(x + 1, y + 1));
                    needMove = false;
                }

                int relativeX = x - targetRectIntersection.getMinX();
                int relativeY = y - targetRectIntersection.getMinY();
                Boolean isOccupied = occupiedGrid.get(relativeX, relativeY);
                if(isOccupied != null && isOccupied){
                    needMove = true;
                } else {
                    writeToSession(" ");
                }
            }
        }

        // Iterate in reverse
        final ListIterator<Drawable> listIterator = drawables.listIterator(drawables.size());
        while (listIterator.hasPrevious()){
            // Get drawable and related positional data
            final Drawable drawable = listIterator.previous();
            final Rect drawableRect = drawable.getRelativeRect();
            final Rect targetIntersection = Rect.intersection(targetRectIntersection, drawableRect);

            // Get drawable caches
            final MutableGrid<@Nullable Character> drawableCharacterCache = drawable.getCharacterCache();
            final MutableGrid<@Nullable SGRSequence> drawableSequenceCache = drawable.getSequenceCache();

            // Validate drawable caches
            if(!drawableCharacterCache.getSize().equals(drawableRect.getSize()) ||
                    !drawableSequenceCache.getSize().equals(drawableRect.getSize())) throw new RectOutOfBoundsException(
                    "The size of the cache does not equal the size of the drawable"
            );

            // Prep a list of SGRStatements that are in effect before the targeted area of the drawable
            final List<@NotNull SGRStatement> prefixStatements = new ArrayList<>();
            // Prefix with RESET
            prefixStatements.add(new SGRStatement(SGRStatement.Type.RESET));
            // Collect statements
            for (SGRSequence sequence : drawableSequenceCache.getElements(
                    new Point(0, 0), Rect.startPointSubtraction(targetIntersection, drawableRect).getStartPoint()
            )) {
                if(sequence != null) prefixStatements.addAll(sequence.toSGRStatements());
            }
            // Compress the statements and combine it into a single SGRSequence
            final SGRSequence prefixSequence = new SGRSequence(SGRSequence.compress(prefixStatements));
            writeToSession(prefixSequence);

            // Print this drawable.
            // This will induce flickering if it's overlaying another drawable,
            // but the old caching system I used was riddled with bugs,
            // and I really needed to progress to the next part in my project...
            for (int y = targetIntersection.getMinY(); y <= targetIntersection.getMaxY(); y++) {
                boolean needMove = true;
                for (int x = targetIntersection.getMinX(); x <= targetIntersection.getMaxX(); x++) {
                    // Convert to relative values
                    final int relativeX = x - drawableRect.getMinX();
                    final int relativeY = y - drawableRect.getMinY();

                    // Move the cursor into position
                    if(needMove){
                        writeToSession(AnsiLib.CUP(x + 1, y + 1));
                        needMove = false;
                    }

                    final Character newCharacter = drawableCharacterCache.get(relativeX, relativeY);
                    final SGRSequence newSequence = drawableSequenceCache.get(relativeX, relativeY);
                    final String newResult = (newSequence == null ? "" : newSequence.toString()) +
                            (newCharacter == null ? " " : newCharacter);

                    // Write
                    writeToSession(newResult);
                }
            }
        }

        // The cursor has moved a lot during the drawing process,
        // so move it back to where it's supposed to be.
        Node focusedNode = tree.getFocusedNode();
        if(focusedNode != null){
            moveTextCursor(focusedNode.getDrawable().getCursorRestingPoint());
            if(focusedNode.getDrawable().cursorEnabled()) writeToSession(AnsiLib.showCursor);
        }
    }
    */

    private List<@NotNull Drawable> getIntersectingImmediateDrawables(@NotNull Rect rect) {
        final List<Drawable> drawables = new ArrayList<>();
        for (Node node : tree.getChildNodes()) {
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
     * Enables the terminal cursor used for text editing.
     *
     * @param caller The caller, which has to be focused.
     */
    public void enableTextCursor(@NotNull Drawable caller) throws NodeNotFoundException {
        if (tree.getFocusedNode() != caller.getNode()) return;
        writeToSession(AnsiLib.showCursor);
    }

    /**
     * Disables the terminal cursor used for text editing.
     *
     * @param caller The caller, which has to be focused.
     */
    public void disableTextCursor(@NotNull Drawable caller) throws NodeNotFoundException {
        if (tree.getFocusedNode() != caller.getNode()) return;
        writeToSession(AnsiLib.hideCursor);
    }

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
                writeToSession(AnsiLib.CSI + "?1000l");
                break;
            case ONLY_BUTTON_PRESS:
                writeToSession(AnsiLib.CSI + "?9h");
                break;
            case NORMAL:
                writeToSession(AnsiLib.CSI + "?1000h");
                break;
            case BUTTON:
                writeToSession(AnsiLib.CSI + "?1002h");
                break;
            case ANY:
                writeToSession(AnsiLib.CSI + "?1003h");
                break;
        }
        System.out.println("changed mouse mode");
        mouseMode = mode;
    }

    void enableDoorwayMode() {
        writeToSession(AnsiLib.CSI + "=255h");
        doorwayModeEnabled = true;
    }

    void disableDoorwayMode() {
        writeToSession(AnsiLib.CSI + "=255l");
        doorwayModeEnabled = false;
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

    synchronized void writeToSession(@NotNull CharSequence characters) {
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
            //fixme remove thisPlugin.getLogger().log(Level.INFO, "Called an event... " + event);

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
    synchronized void performBroadcast(@NotNull WMEvent event) {
        thisPlugin.getLogger().log(Level.INFO, "Performing broadcast ..." + event);

        List<Node> childNodes = tree.getAllNodes();
        for (Node node : childNodes) {
            AccelaAPI.getPluginManager().callEvent(event, node.drawable.getChannel());
        }

        /*
        List<Node> childNodes = tree.getChildNodes();
        for (Node node : childNodes) {
            // A list of drawables to send the events to
            List<Drawable> drawableList = new ArrayList<>();
            drawableList.add(node.getDrawable());

            while (drawableList.size() > 0) {
                Drawable subDrawable = drawableList.remove(0);

                // Send the event to the drawable
                EventChannel channel = subDrawable.getChannel();
                AccelaAPI.getPluginManager().callEvent(event, channel);
                //fixme remove thisPlugin.getLogger().log(Level.INFO, "Called an event... " + event);

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
         */
    }

    /**
     * Broadcasts an {@link Event} to all {@link Drawable}s
     *
     * @param event The event to broadcast
     */
    public synchronized void broadcastEvent(final @NotNull WMEvent event) {
        //thisPlugin.getLogger().log(Level.INFO, "Received event..." + event);
        // Focus mods etc
        // todo make shortcuts customizable
        if (event instanceof SpecialInputEvent) {
            SpecialInputEvent specialInputEvent = (SpecialInputEvent) event;

            if (specialInputEvent.getKey() == SpecialInputEvent.SpecialKey.HT) {
                Node focusedNode = tree.getFocusedNode();
                if (focusedNode != null) {
                    List<Node> nodes = tree.getChildNodes();

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
            // fixme Can probably be simplified to just "intersectingChildNodes.size() > index"
            while (intersectingChildNodes.size() > 0 && intersectingChildNodes.size() > index) {
                focusNode = intersectingChildNodes.get(index);

                if (!focusNode.getDrawable().wantsFocus()) {
                    index++;
                    continue;
                }

                if (focusNode instanceof Branch) {
                    Branch branch = (Branch) focusNode;

                    pointRect = Rect.startPointAddition(
                            pointRect, focusNode.getDrawable().getRelativeRect().getStartPoint());

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
