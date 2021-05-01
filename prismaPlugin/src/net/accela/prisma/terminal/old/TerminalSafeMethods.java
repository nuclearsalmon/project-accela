package net.accela.prisma.terminal.old;

import net.accela.prisma.gui.geometry.Size;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.List;

public interface TerminalSafeMethods {
    @NotNull Size getSize();

    boolean supportsAixtermColor();

    boolean supportsTableColor();

    boolean supportsTrueColor();

    boolean supportsIceColor();

    @NotNull List<@NotNull Charset> getSupportedCharsets();

    @NotNull Charset getCharset();

    boolean supportsFontChange();
}
