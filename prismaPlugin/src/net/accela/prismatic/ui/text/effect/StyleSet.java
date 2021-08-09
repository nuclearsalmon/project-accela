package net.accela.prismatic.ui.text.effect;

import net.accela.prismatic.sequence.SGRAttribute;
import net.accela.prismatic.sequence.SGRStatement;
import net.accela.prismatic.ui.text.color.TextColor;

import java.util.Arrays;
import java.util.EnumSet;

public interface StyleSet<T extends StyleSet<T>> {
    /**
     * Returns the current background color
     *
     * @return Current background color
     */
    TextColor getBackgroundColor();

    /**
     * Updates the current background color
     *
     * @param backgroundColor New background color
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T setBackgroundColor(TextColor backgroundColor);

    /**
     * Returns the current foreground color
     *
     * @return Current foreground color
     */
    TextColor getForegroundColor();

    /**
     * Updates the current foreground color
     *
     * @param foregroundColor New foreground color
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T setForegroundColor(TextColor foregroundColor);

    /**
     * Returns the current underline color
     *
     * @return Current underline color
     */
    TextColor.RGB getUnderlineColor();

    /**
     * Updates the current underline color
     *
     * @param underlineColor New underline color
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T setUnderlineColor(TextColor.RGB underlineColor);

    /**
     * Returns the current terminal font
     *
     * @return Current terminal font
     */
    TerminalFont getTerminalFont();

    /**
     * Updates the current terminal font
     *
     * @param font New terminal font
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T setTerminalFont(TerminalFont font);

    /**
     * Adds zero or more modifiers to the set of currently active modifiers
     *
     * @param modifiers Modifiers to add to the set of currently active modifiers
     * @return Itself
     */

    @SuppressWarnings("UnusedReturnValue")
    T enableModifiers(TextEffect... modifiers);

    /**
     * Removes zero or more modifiers from the set of currently active modifiers
     *
     * @param modifiers Modifiers to remove from the set of currently active modifiers
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T disableModifiers(TextEffect... modifiers);

    /**
     * Sets the active modifiers to exactly the set passed in to this method. Any previous state of which modifiers are
     * enabled doesn't matter.
     *
     * @param modifiers Modifiers to set as active
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T setModifiers(EnumSet<TextEffect> modifiers);

    /**
     * Removes all active modifiers
     *
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T clearModifiers();

    /**
     * Removes all active modifiers, colors and other parameters
     *
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T clearAll();

    /**
     * Returns all the TextEffect codes that are currently active
     *
     * @return Currently active TextEffect modifiers
     */
    EnumSet<TextEffect> getActiveModifiers();

    /**
     * copy colors and set of TextEffect codes
     *
     * @param source Modifiers to set as active
     * @return Itself
     */
    @SuppressWarnings("UnusedReturnValue")
    T setStyleFrom(StyleSet<?> source);


    class Set implements StyleSet<Set> {
        private TextColor foregroundColor;
        private TextColor backgroundColor;
        private TextColor.RGB underlineColor;
        private TerminalFont font;
        private final EnumSet<TextEffect> style = EnumSet.noneOf(TextEffect.class);

        public Set() {
        }

        public Set(StyleSet<?> source) {
            setStyleFrom(source);
        }

        @Override
        public TextColor getBackgroundColor() {
            return backgroundColor;
        }

        @Override
        public Set setBackgroundColor(TextColor backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        @Override
        public TextColor getForegroundColor() {
            return foregroundColor;
        }

        @Override
        public Set setForegroundColor(TextColor foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        @Override
        public TextColor.RGB getUnderlineColor() {
            return underlineColor;
        }

        @Override
        public Set setUnderlineColor(TextColor.RGB underlineColor) {
            this.underlineColor = underlineColor;
            return this;
        }

        @Override
        public TerminalFont getTerminalFont() {
            return font;
        }

        @Override
        public Set setTerminalFont(TerminalFont font) {
            this.font = font;
            return this;
        }

        @Override
        public Set enableModifiers(TextEffect... modifiers) {
            style.addAll(Arrays.asList(modifiers));
            return this;
        }

        @Override
        public Set disableModifiers(TextEffect... modifiers) {
            style.removeAll(Arrays.asList(modifiers));
            return this;
        }

        @Override
        public Set setModifiers(EnumSet<TextEffect> modifiers) {
            style.clear();
            style.addAll(modifiers);
            return this;
        }

        @Override
        public Set clearModifiers() {
            style.clear();
            return this;
        }

        @Override
        public Set clearAll() {
            style.clear();
            foregroundColor = null;
            backgroundColor = null;
            return this;
        }

        @Override
        public EnumSet<TextEffect> getActiveModifiers() {
            return EnumSet.copyOf(style);
        }

        @Override
        public Set setStyleFrom(StyleSet<?> source) {
            setBackgroundColor(source.getBackgroundColor());
            setForegroundColor(source.getForegroundColor());
            setModifiers(source.getActiveModifiers());
            return this;
        }


        public Set consume(SGRStatement[] statements) {
            for (SGRStatement statement : statements) {
                consume(statement);
            }
            return this;
        }

        public Set consume(java.util.Set<SGRStatement> statementSet) {
            return consume(statementSet.toArray(SGRStatement[]::new));
        }

        public Set consume(Iterable<SGRStatement> statements) {
            for (SGRStatement statement : statements) {
                consume(statement);
            }
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Set consume(SGRStatement statement) {
            SGRAttribute attribute = statement.getAttribute();
            TextEffect textEffect = TextEffect.fromSGRAttribute(attribute);
            switch (attribute) {
                case RESET:
                    clearAll();
                    break;
                case INTENSITY_BRIGHT_OR_BOLD:
                case INTENSITY_DIM_OR_THIN:
                case EMPHASIS_ITALIC:
                case EMPHASIS_FRAKTUR:
                case INVERT_ON:
                case CONCEAL_ON:
                case STRIKE_THROUGH_ON:
                case BLINK_SLOW:
                case BLINK_FAST:
                case UNDERLINE_SINGLE:
                case UNDERLINE_DOUBLE:
                    enableModifiers(textEffect);
                    break;
                case INTENSITY_OFF:
                case EMPHASIS_OFF:
                case INVERT_OFF:
                case CONCEAL_OFF:
                case STRIKE_THROUGH_OFF:
                case BLINK_OFF:
                case UNDERLINE_OFF:
                    disableModifiers(textEffect);
                    break;
                case SUBSCRIPT:
                    disableModifiers(TextEffect.SUPERSCRIPT);
                    enableModifiers(TextEffect.SUBSCRIPT);
                case SUPERSCRIPT:
                    disableModifiers(TextEffect.SUBSCRIPT);
                    enableModifiers(TextEffect.SUPERSCRIPT);
                    break;
                case UNDERLINE_COLOR:
                    setUnderlineColor((TextColor.RGB) statement.toColor());
                case UNDERLINE_COLOR_DEFAULT:
                    setUnderlineColor(null);
                    break;
                case FONT_1:
                case FONT_2:
                case FONT_3:
                case FONT_4:
                case FONT_5:
                case FONT_6:
                case FONT_7:
                case FONT_8:
                case FONT_9:
                case FONT_DEFAULT:
                    setTerminalFont(TerminalFont.fromSGRAttribute(attribute));
                    break;
                case FG_BLK:
                case FG_RED:
                case FG_GRN:
                case FG_YEL:
                case FG_BLU:
                case FG_MAG:
                case FG_CYA:
                case FG_WHI:
                case FG_RGB:
                case FG_DEFAULT:
                case FG_BLK_BRIGHT:
                case FG_RED_BRIGHT:
                case FG_GRN_BRIGHT:
                case FG_YEL_BRIGHT:
                case FG_BLU_BRIGHT:
                case FG_MAG_BRIGHT:
                case FG_CYA_BRIGHT:
                case FG_WHI_BRIGHT:
                    setForegroundColor(statement.toColor());
                    break;
                case BG_BLK:
                case BG_RED:
                case BG_GRN:
                case BG_YEL:
                case BG_BLU:
                case BG_MAG:
                case BG_CYA:
                case BG_WHI:
                case BG_RGB:
                case BG_DEFAULT:
                case BG_BLK_BRIGHT:
                case BG_RED_BRIGHT:
                case BG_GRN_BRIGHT:
                case BG_YEL_BRIGHT:
                case BG_BLU_BRIGHT:
                case BG_MAG_BRIGHT:
                case BG_CYA_BRIGHT:
                case BG_WHI_BRIGHT:
                    setBackgroundColor(statement.toColor());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown SGRStatement, not implemented yet");
            }
            return this;
        }
    }
}