package net.accela.prismatic.input.events.mouse;

import net.accela.prismatic.input.InputType;
import net.accela.prismatic.input.lanterna.actions.InputEvent;
import net.accela.prismatic.ui.geometry.Point;
import org.jetbrains.annotations.NotNull;

/**
 * Represents mouse input as an {@link InputEvent}, containing more information than a standard InputEvent.
 * For example you can query the mouse cursor position, which button was clicked (if any).
 * It also supports various kinds of {@link MouseActionType}s, such as clicking, moving, dragging or scrolling.
 */
public class MouseInputEvent extends InputEvent {
    private final MouseActionType actionType;
    private final int button;
    private final Point position;

    /**
     * Constructs a MouseInputEvent based on an action type, a button and a location on the screen
     *
     * @param actionType The kind of {@link MouseActionType} this event is referring to
     * @param button     The button involved
     * @param position   Where the mouse cursor is positioned
     */
    public MouseInputEvent(MouseActionType actionType, int button, Point position) {
        super(InputType.MouseEvent, false, false);
        this.actionType = actionType;
        this.button = button;
        this.position = position;
    }

    /**
     * What kind of {@link MouseActionType} this event is referring to
     *
     * @return The {@link MouseActionType} of the mouse event
     */
    public MouseActionType getActionType() {
        return actionType;
    }

    /**
     * The button which was clicked at the time of this event.
     * For {@link MouseActionType#CLICK_RELEASE}events, there's no button
     * information available and this will return 0.
     *
     * @return The button which was clicked at the time of this event
     */
    public int getButtonAsInt() {
        return button;
    }

    /**
     * The button which was clicked at the time of this event.
     * For {@link MouseActionType#CLICK_RELEASE} events, there's no button
     * information available and this will return {@link MouseButton#NONE}.
     *
     * @return The button which was clicked at the time of this event
     */
    public @NotNull MouseButton getButtonAsEnum() {
        return MouseButton.fromIndex(getButtonAsInt());
    }

    /**
     * The position of the mouse cursor when this event was generated.
     *
     * @return Position of the mouse cursor
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Whether a mouse button was held down.
     * If the {@link MouseActionType} was {@link MouseActionType#CLICK_DOWN}.
     *
     * @return Whether a mouse button was held down
     */
    public boolean isMouseDown() {
        return actionType == MouseActionType.CLICK_DOWN;
    }

    /**
     * Whether the mouse cursor was held down whilst moving
     * If the {@link MouseActionType} was {@link MouseActionType#DRAG}.
     *
     * @return Whether the mouse cursor was held down whilst moving
     */
    public boolean isMouseDrag() {
        return actionType == MouseActionType.DRAG;
    }

    /**
     * Whether the mouse cursor was moved with no clicking
     * If the {@link MouseActionType} was {@link MouseActionType#MOVE}.
     *
     * @return Whether the mouse cursor was moved with no clicking
     */
    public boolean isMouseMove() {
        return actionType == MouseActionType.MOVE;
    }

    /**
     * Whether a mouse button was released.
     * If the {@link MouseActionType} was {@link MouseActionType#CLICK_RELEASE}.
     *
     * @return Whether a mouse button was released
     */
    public boolean isMouseRelease() {
        return actionType == MouseActionType.CLICK_RELEASE;
    }

    @Override
    public String toString() {
        return "MouseInputEvent{actionType=" + actionType + ", button=" + button + ", position=" + position + '}';
    }
}
