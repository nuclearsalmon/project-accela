package net.accela.prismatic.input.events.mouse;

import org.jetbrains.annotations.NotNull;

/**
 * Represents mouse buttons as an enum.
 */
public enum MouseButton {
    /**
     * No button: 0
     */
    NONE,

    /**
     * The left button: 1
     */
    LEFT,

    /**
     * The middle (usually scroll wheel) button: 2
     */
    MIDDLE,

    /**
     * The right button: 3
     */
    RIGHT,

    /**
     * Scrolling up with the scroll wheel: 4
     */
    WHEEL_UP,

    /**
     * Scrolling down with the scroll wheel:5
     */
    WHEEL_DOWN,
    ;

    @NotNull
    public static MouseButton fromIndex(int index) {
        return MouseButton.values()[index]; // todo This is far from ideal, but it works.
    }
}
