package net.accela.prisma.session;

import org.jetbrains.annotations.NotNull;

public class TerminalAccessor {
    final Terminal terminal;

    public TerminalAccessor(@NotNull Terminal terminal) {
        this.terminal = terminal;
    }
}
