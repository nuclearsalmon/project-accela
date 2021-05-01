package net.accela.prisma.terminal.old;

import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Called when receiving String keyboard input
 */
@Deprecated
public class StringInputEvent extends InputEvent {
    final String entry;
    final boolean meta, control, alt, shift;

    public StringInputEvent(@NotNull Plugin caller, @NotNull String entry) {
        super(caller);
        this.entry = entry;
        this.meta = false;
        this.control = false;
        this.alt = false;
        this.shift = false;
    }

    public StringInputEvent(@NotNull Plugin caller, @NotNull String entry,
                            boolean meta,
                            boolean control,
                            boolean alt,
                            boolean shift) {
        super(caller);
        this.entry = entry;
        this.meta = meta;
        this.control = control;
        this.alt = alt;
        this.shift = shift;
    }

    @NotNull
    public String getEntry() {
        return entry;
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
    public boolean equals(Object obj) {
        if (obj instanceof StringInputEvent) {
            StringInputEvent inputObj = (StringInputEvent) obj;

            return toString().equals(inputObj.toString())
                    && isMeta() == inputObj.isMeta()
                    && isControl() == inputObj.isControl()
                    && isAlt() == inputObj.isAlt()
                    && isShift() == inputObj.isShift();
        }
        return false;
    }

    @NotNull
    @Override
    public String toString() {
        return this.getClass().getName() +
                "[entry=" + entry + ",meta=" + meta + ",control=" + control + ",alt=" + alt + ",shift=" + shift + "]";
    }
}
