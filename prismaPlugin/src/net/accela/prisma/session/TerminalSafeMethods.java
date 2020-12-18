package net.accela.prisma.session;

import net.accela.prisma.geometry.Size;
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
}
