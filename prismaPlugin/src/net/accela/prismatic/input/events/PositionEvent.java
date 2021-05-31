package net.accela.prismatic.input.events;

import net.accela.prismatic.input.InputType;
import net.accela.prismatic.input.lanterna.actions.InputEvent;
import net.accela.prismatic.ui.geometry.Point;

/**
 * Represents the reported position of the terminal cursor.
 * Only used internally when the position is queried.
 */
public class PositionEvent extends InputEvent {
    private final Point position;

    /**
     * Constructs a {@link PositionEvent} based on a position on the screen
     *
     * @param position the {@link Point} reported
     */
    public PositionEvent(Point position) {
        super(InputType.CursorPoint);
        this.position = position;
    }

    /**
     * The position reported (where the terminal cursor is resting).
     *
     * @return The position reported
     */
    public Point getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{position=" + position + '}';
    }
}
