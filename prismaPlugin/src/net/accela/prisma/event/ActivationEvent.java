package net.accela.prisma.event;

import net.accela.prisma.gui.Drawable;
import net.accela.prisma.gui.DrawableIdentifier;
import net.accela.server.event.Event;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Broadcast when a {@link Drawable} is activated.
 * <br>
 * It is expected that all {@link Drawable}s excluding the target either turn inactive,
 * or stay active if they wish to snoop in on input events that might not be aimed at them specifically.
 */
public class ActivationEvent extends PluginRequiredEvent {
    final DrawableIdentifier target;

    /**
     * @param caller The {@link Plugin} that is instantiating this {@link Event}.
     * @param target The {@link DrawableIdentifier} that is about to become activated.
     *               If null, then all {@link Drawable}s are expected to be activated.
     */
    public ActivationEvent(@NotNull Plugin caller, @Nullable DrawableIdentifier target) {
        super(caller);
        this.target = target;
    }

    /**
     * @return The {@link Drawable} that is about to become activated.
     * If null, then all {@link Drawable}s are expected to be activated.
     */
    public @Nullable DrawableIdentifier getTarget() {
        return target;
    }
}
