package net.accela.prismatic.input;

import net.accela.prismatic.input.events.mouse.MouseInputEvent;
import net.accela.prismatic.input.lanterna.actions.InputEvent;
import net.accela.server.event.Event;

/**
 * A collection of various input types
 */
public enum InputType {
    //
    // Physical keys
    //

    /**
     * Represents any characters that aren't special keys such as {@link InputType#Enter}.
     * Typically this is used to read keyboard input.
     */
    Character,
    // Special characters
    Escape,
    Backspace,
    ArrowLeft,
    ArrowRight,
    ArrowUp,
    ArrowDown,
    Insert,
    Delete,
    Home,
    End,
    PageUp,
    PageDown,
    Tab,
    ReverseTab,
    Enter,
    // Function keys F1-F20
    F1,
    F2,
    F3,
    F4,
    F5,
    F6,
    F7,
    F8,
    F9,
    F10,
    F11,
    F12,
    F13,
    F14,
    F15,
    F16,
    F17,
    F18,
    F19,
    F20,
    /**
     * Any unknown input that was not able to be decoded.
     * Actually encountering this {@link InputType} is unlikely and the behaviour is undefined.
     */
    Unknown,

    //
    // Virtual keys
    //

    /**
     * Represents a cursor position on screen. This is only used internally,
     * typically to calculate the terminal size and unicode support.
     */
    CursorPoint,
    /**
     * Any mouse events, such as movement and clicks.
     * Note that to listen for this kind of event you should cast
     * the {@link InputEvent} into a {@link MouseInputEvent} first,
     * this will let you retrieve more {@link Event}-specific information.
     */
    MouseEvent,
    /**
     * Represents a closed input stream.
     * If trying to read from such a stream then this {@link InputType} will get returned.
     */
    EOF,
}
