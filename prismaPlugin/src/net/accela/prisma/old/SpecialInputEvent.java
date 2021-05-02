package net.accela.prisma.terminal.old;

import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Called when receiving special keyboard input
 */
@Deprecated
public class SpecialInputEvent extends InputEvent {
    SpecialKey key;
    boolean meta = false, control = false, alt = false, shift = false;

    public SpecialInputEvent(@NotNull Plugin caller, @NotNull SpecialKey key) {
        super(caller);
        this.key = key;
    }

    public SpecialInputEvent(@NotNull Plugin caller, @NotNull SpecialKey key, boolean meta, boolean control, boolean alt, boolean shift) {
        super(caller);
        this.key = key;
        this.meta = meta;
        this.control = control;
        this.alt = alt;
        this.shift = shift;
    }

    // Special keycodes
    @SuppressWarnings("unused")
    public enum SpecialKey {
        UP, DOWN, RIGHT, LEFT, KEYPAD_5, HOME, INSERT, DELETE, END, PGUP, PGDN,
        F0, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, F17, F18, F19, F20,
        // Control characters
        NUL, SOH, STX, ETX, EOT, ENQ, ACK, BEL, BS, HT, LF, VT, FF, CR, SO, SI, DLE, DC1, DC2, DC3, DC4, NAK, SYN,
        ETB, CAN, EM, SUB, ESC, FS, GS, RS, US, DEL
    }

    @NotNull
    public SpecialKey getKey() {
        return key;
    }

    public boolean isMeta() {
        return meta;
    }

    public boolean isControl() {
        return control;
    }

    public boolean isAlt() {
        return alt;
    }

    public boolean isShift() {
        return shift;
    }

    @Override
    public String toString() {
        return this.getClass().getName() +
                "[key=" + key + ",meta=" + meta + ",control=" + control + ",alt=" + alt + ",shift=" + shift + "]";
    }
}
