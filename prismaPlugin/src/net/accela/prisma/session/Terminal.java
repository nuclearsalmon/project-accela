package net.accela.prisma.session;

import net.accela.prisma.geometry.Size;
import net.accela.server.AccelaAPI;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Terminal {
    // Constants - Size
    public final static Size DEFAULT_SIZE = new Size(80, 24);

    // Constants - Charset
    public final static Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    public final static Charset ASCII_CHARSET = StandardCharsets.US_ASCII;
    public final static Charset IBM437_CHARSET = Charset.forName("IBM437");
    public final static Charset DEFAULT_CHARSET = UTF8_CHARSET;
    public final static List<Charset> DEFAULT_SUPPORTED_CHARSETS = new ArrayList<>() {{
        add(Terminal.UTF8_CHARSET);
        add(Terminal.IBM437_CHARSET);
        add(Terminal.ASCII_CHARSET);
    }};

    // Constants - Colors
    public final static boolean DEFAULT_AIXTERM_COLOR_SUPPORT = true;
    public final static boolean DEFAULT_TABLE_COLOR_SUPPORT = true;
    public final static boolean DEFAULT_TRUE_COLOR_SUPPORT = true;
    public final static boolean DEFAULT_ICE_COLOR_SUPPORT = true;

    // Size
    protected @NotNull Size terminalSize = DEFAULT_SIZE;

    // Charset
    /**
     * Order matters. Preferred ones go first.
     */
    protected final @NotNull List<@NotNull Charset> supportedCharsets = DEFAULT_SUPPORTED_CHARSETS;
    protected @NotNull Charset charset = DEFAULT_CHARSET;

    // Colors
    protected boolean supportsAixtermColor = DEFAULT_AIXTERM_COLOR_SUPPORT;
    protected boolean supports8BitColor = DEFAULT_TABLE_COLOR_SUPPORT;
    protected boolean supports24BitColor = DEFAULT_TRUE_COLOR_SUPPORT;
    protected boolean supportsICEColor = DEFAULT_ICE_COLOR_SUPPORT;

    public Terminal() {
    }

    //
    // Getters
    //

    public @NotNull Size getSize() {
        return terminalSize;
    }

    public boolean getAixtermColorSupport() {
        return supportsAixtermColor;
    }

    public boolean get8BitColorSupport() {
        return supports8BitColor;
    }

    public boolean get24BitColorSupport() {
        return supports24BitColor;
    }

    public boolean getIceColorSupport() {
        return supportsICEColor;
    }

    public @NotNull List<@NotNull Charset> getSupportedCharsets() {
        return supportedCharsets;
    }

    public @NotNull Charset getCharset() {
        return charset;
    }

    //
    // Setters
    //

    public void setSize(@NotNull Size size) {
        terminalSize = size;
    }

    public void setCharset(@NotNull Charset charset) {
        if (supportedCharsets.contains(charset)) {
            AccelaAPI.getLogger().log(Level.INFO, "Changing charset to " + charset.name());
            this.charset = charset;
        } else {
            throw new UnsupportedCharsetException(charset.name());
        }
    }

    public void addCharsetSupport(@NotNull Charset charset) {
        synchronized (supportedCharsets) {
            if (!supportedCharsets.contains(charset)) supportedCharsets.add(charset);
        }
    }

    public void removeCharsetSupport(@NotNull Charset charset) {
        supportedCharsets.remove(charset);
    }
}
