package net.accela.prismatic.behaviour;

import net.accela.prismatic.util.chars.C0ControlCode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

/**
 * <a href="https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h2-Mouse-Tracking">
 * invisible-island.net</a>
 */
public enum MouseCaptureMode {
    CLICK(9),                      // CSI ?9h
    CLICK_NETRUNNER(1000),         // CSI ?1000h
    CLICK_SYNCTERM(1000),          // CSI ?1000h
    CLICK_RELEASE(1000),           // CSI ?1000h
    CLICK_RELEASE_DRAG(1002),      // CSI ?1002h
    CLICK_RELEASE_DRAG_MOVE(1003), // CSI ?1003h
    XTERM_EXTENDED(1005),          // CSI ?1005h
    SRG_EXTENDED(1006),            // CSI ?1006h
    URXVT_EXTENDED(1015),          // CSI ?1015h
    PIXEL(1016),                   // CSI ?1016h
    ;

    final int index;

    MouseCaptureMode(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getEnablingSequence(@NotNull Charset charset) {
        return getSequence(true, charset);
    }

    public byte[] getDisablingSequence(@NotNull Charset charset) {
        return getSequence(false, charset);
    }

    public byte[] getSequence(boolean enable, @NotNull Charset charset) {
        return (C0ControlCode.ESC + "[?" + index + (enable ? "h" : "l")).getBytes(charset);
    }
}
