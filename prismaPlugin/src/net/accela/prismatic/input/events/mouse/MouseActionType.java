package net.accela.prismatic.input.events.mouse;

/**
 * Represents different kinds of mouse input.
 */
public enum MouseActionType {
    /**
     * Pressing a button
     */
    CLICK_DOWN,
    /**
     * Releasing a button after having pressed it
     */
    CLICK_RELEASE,
    /**
     * Using the scroll wheel to scroll up
     */
    SCROLL_UP,
    /**
     * Using the scroll wheel to scroll down
     */
    SCROLL_DOWN,
    /**
     * Moving the mouse cursor on the screen while holding a button down
     */
    DRAG,
    /**
     * Moving the mouse cursor on the screen without holding any buttons down
     */
    MOVE,
}
