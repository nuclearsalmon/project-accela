package net.accela.prismatic.ui.text.effect;

import net.accela.prismatic.sequence.SGRAttribute;
import net.accela.prismatic.sequence.annotation.Inconsistent;
import net.accela.prismatic.sequence.annotation.NonStandard;
import net.accela.prismatic.sequence.annotation.NotWidelySupported;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A couple of effects that can be applied to text, usually on a per-character basis.
 * This borrows from (and is somewhat intercompatible with) ANSI SGR codes,
 * but it is important to note that they are NOT the same thing.<br><br>
 * <p>
 * For raw low-level mode or the actual SGR codes, refer to {@link SGRAttribute}.<br><br>
 * <p>
 * Some noteworthy differences between {@link SGRAttribute} and {@link TextEffect}:
 * <ul>
 *     <li>{@link TextEffect} is simplified, meaning it does not contain all {@link SGRAttribute}s.</li>
 *     <li>{@link TextEffect} excludes color sequences.</li>
 *     <li>{@link TextEffect} does not specify "_ON"/"_OFF" methods.</li>
 *     <li>{@link TextEffect} can have custom extensions which are not a part of the SGR spec
 *     and are only used in the GUI.</li>
 * </ul>
 */
public enum TextEffect {
    // Color Intensity
    /**
     * Enables Bright color intensity OR bold font faces (this can vary between terminals).
     *
     * @see SGRAttribute#INTENSITY_DIM_OR_THIN
     * @see SGRAttribute#INTENSITY_OFF
     */
    @Inconsistent(reason = "Bright or bold")
    INTENSITY_BRIGHT_OR_BOLD(SGRAttribute.INTENSITY_BRIGHT_OR_BOLD, SGRAttribute.INTENSITY_OFF),

    /**
     * Enables dim color intensity OR faint font faces (this can vary between terminals).
     *
     * @see SGRAttribute#INTENSITY_BRIGHT_OR_BOLD
     * @see SGRAttribute#INTENSITY_OFF
     */
    @Inconsistent(reason = "Dim or faint/thin")
    INTENSITY_DIM_OR_THIN(SGRAttribute.INTENSITY_DIM_OR_THIN, SGRAttribute.INTENSITY_OFF),

    // Emphasis
    /**
     * Enables italic emphasis. Not widely supported. Sometimes treated as an inverse or blink effect.
     *
     * @see SGRAttribute#EMPHASIS_FRAKTUR
     * @see SGRAttribute#EMPHASIS_OFF
     */
    @NotWidelySupported
    @Inconsistent(reason = "Sometimes treated as inverse or blink")
    EMPHASIS_ITALIC(SGRAttribute.EMPHASIS_ITALIC, SGRAttribute.EMPHASIS_OFF),

    /**
     * Enables fraktur emphasis. Rarely supported.
     *
     * @see SGRAttribute#EMPHASIS_ITALIC
     * @see SGRAttribute#EMPHASIS_OFF
     */
    @NotWidelySupported
    EMPHASIS_FRAKTUR(SGRAttribute.EMPHASIS_FRAKTUR, SGRAttribute.EMPHASIS_OFF),

    // Inversion
    /**
     * Enables color inversion by swapping the foreground and background colors.
     * Also known as "reverse video". Inconsistent emulation.
     *
     * @see SGRAttribute#INVERT_OFF
     */
    @Inconsistent(reason = "Inconsistent emulation")
    INVERT(SGRAttribute.INVERT_ON, SGRAttribute.INVERT_OFF),

    // Concealment
    /**
     * Enables text concealing.
     * Also known as "Hide". Not widely supported.
     *
     * @see SGRAttribute#CONCEAL_OFF
     */
    @NotWidelySupported
    CONCEAL(SGRAttribute.CONCEAL_ON, SGRAttribute.CONCEAL_OFF),

    // Strikethrough // Crossed out
    /**
     * Enables a strike-through line across text. Also known as "Crossed out".
     * Makes characters legible, but marked as if for deletion.
     *
     * @see SGRAttribute#STRIKE_THROUGH_OFF
     */
    @NotWidelySupported
    STRIKE_THROUGH(SGRAttribute.STRIKE_THROUGH_ON, SGRAttribute.STRIKE_THROUGH_OFF),

    // Cursor blinking
    /**
     * Enables slow cursor blinking. Less than 150 blinks per minute, according to the spec.
     *
     * @see SGRAttribute#BLINK_FAST
     * @see SGRAttribute#BLINK_OFF
     */
    BLINK_SLOW(SGRAttribute.BLINK_SLOW, SGRAttribute.BLINK_OFF),

    /**
     * Enables fast cursor blinking. More than 150 blinks per minute, according to the spec.
     *
     * @see SGRAttribute#BLINK_SLOW
     * @see SGRAttribute#BLINK_OFF
     */
    @NotWidelySupported
    BLINK_FAST(SGRAttribute.BLINK_FAST, SGRAttribute.BLINK_OFF),

    // Underlines
    /**
     * Enables a single underline.
     *
     * @see SGRAttribute#UNDERLINE_DOUBLE
     * @see SGRAttribute#UNDERLINE_OFF
     */
    UNDERLINE_SINGLE(SGRAttribute.UNDERLINE_SINGLE, SGRAttribute.UNDERLINE_OFF),

    /**
     * Enables a double underline.
     *
     * @see SGRAttribute#UNDERLINE_SINGLE
     * @see SGRAttribute#UNDERLINE_OFF
     */
    @Inconsistent(reason = "Sometimes used as bold off")
    UNDERLINE_DOUBLE(SGRAttribute.UNDERLINE_DOUBLE, SGRAttribute.UNDERLINE_OFF),

    // Subscript and superscript
    /**
     * Enables subscript. Not widely supported, not in standard. Supported in mintty.
     */
    @NotWidelySupported
    @NonStandard(info = "Extension by mintty, not in standard.")
    SUBSCRIPT(SGRAttribute.SUBSCRIPT, SGRAttribute.SUPERSCRIPT),

    /**
     * Enables superscript. Not widely supported, not in standard. Supported in mintty.
     */
    @NotWidelySupported
    @NonStandard(info = "Extension by mintty, not in standard.")
    SUPERSCRIPT(SGRAttribute.SUPERSCRIPT, SGRAttribute.SUBSCRIPT);

    //
    // Constructor and methods
    //

    @NotNull
    private static final Map<@NotNull SGRAttribute, @NotNull TextEffect> generatedConversionMap = new HashMap<>();

    private final @Nullable SGRAttribute enabledSGR;
    private final @Nullable SGRAttribute disabledSGR;
    private final boolean isCustomExtension;

    // Support for custom extensions, currently unused.
    @SuppressWarnings("unused")
    TextEffect() {
        isCustomExtension = true;
        enabledSGR = null;
        disabledSGR = null;
    }

    TextEffect(@NotNull SGRAttribute enabledSGR,
               @NotNull SGRAttribute disabledSGR) {
        isCustomExtension = false;
        this.enabledSGR = enabledSGR;
        this.disabledSGR = disabledSGR;
    }

    /**
     * @return Whether this is a custom GUI extension or not
     */
    public boolean isCustomExtension() {
        return isCustomExtension;
    }

    /**
     * This will throw an exception if you attempt to execute it on a custom extension.
     */
    @NotNull
    public SGRAttribute getEnabledSGRAttribute() {
        if (isCustomExtension()) {
            throw new UnsupportedOperationException("A custom extension cannot be converted to an SGRAttribute.");
        }
        return Objects.requireNonNull(enabledSGR);
    }

    /**
     * This will throw an exception if you attempt to execute it on a custom extension.
     */
    @NotNull
    public SGRAttribute getDisabledSGRAttribute() {
        if (isCustomExtension()) {
            throw new UnsupportedOperationException("A custom extension cannot be converted to an SGRAttribute.");
        }
        return Objects.requireNonNull(disabledSGR);
    }

    /**
     * This will throw an exception if you attempt to execute it on a custom extension.
     */
    @NotNull
    public SGRAttribute getSGRAttribute(boolean enabled) {
        if (isCustomExtension()) {
            throw new UnsupportedOperationException("A custom extension cannot be converted to an SGRAttribute.");
        }
        return Objects.requireNonNull(enabled ? enabledSGR : disabledSGR);
    }

    /**
     * Attempts to return the {@link TextEffect} equivalent of an {@link SGRAttribute}.
     * NOTE: This will NOT preserve polarity (disable vs enable, etc)!
     *
     * @param attribute The {@link SGRAttribute} to try and convert.
     * @return The {@link TextEffect} equivalent, if any. {@code null} if none is found.
     */
    @Nullable
    public static TextEffect fromSGRAttribute(@NotNull SGRAttribute attribute) {
        synchronized (generatedConversionMap) {
            if (generatedConversionMap.size() == 0) {
                for (TextEffect value : values()) {
                    if (value.isCustomExtension()) continue;

                    if (value.enabledSGR == null || value.disabledSGR == null)
                        throw new IllegalArgumentException("SGRs must not be null");

                    generatedConversionMap.put(value.enabledSGR, value);
                    generatedConversionMap.put(value.disabledSGR, value);
                }
            }
        }
        return generatedConversionMap.get(attribute);
    }
}
