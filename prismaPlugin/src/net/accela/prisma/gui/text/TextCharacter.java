package net.accela.prisma.gui.text;

import net.accela.prisma.gui.text.color.TextColor;
import net.accela.prisma.gui.text.effect.TextEffect;
import net.accela.prisma.util.TerminalTextUtils;
import net.accela.prisma.util.WCWidth;
import org.jetbrains.annotations.NotNull;

import java.text.BreakIterator;
import java.util.*;

/**
 * Represents a single character, with additional metadata such as colors and modifiers.
 * The actual character is stored as a string, allowing you to use multiple codepoints.
 * This class is immutable.
 */
public class TextCharacter {
    @SuppressWarnings("unused")
    public static final TextCharacter DEFAULT =
            new TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);

    /**
     * The "character" might not fit in a Java 16-bit char (this applies to emoji and other types)
     * so we store it in a String instead.
     */
    private final String character;
    private final TextColor foregroundColor;
    private final TextColor backgroundColor;
    private final EnumSet<TextEffect> modifiers; // This isn't immutable, but we should treat it as such and not expose it!

    //
    // Constructor and factory methods
    //

    // Constructors

    /**
     * Creates a new {@link TextCharacter} based on a physical character,
     * color information and optional modifiers.
     *
     * @param character       Character to refer to
     * @param foregroundColor Foreground color the character has
     * @param backgroundColor Background color the character has
     * @param styles          Optional list of modifiers to apply when drawing the character
     */
    private TextCharacter(
            char character,
            TextColor foregroundColor,
            TextColor backgroundColor,
            @NotNull TextEffect... styles) {
        this(character, foregroundColor, backgroundColor, toEnumSet(styles));
    }

    /**
     * Creates a new {@link TextCharacter} based on a physical character,
     * color information and a set of modifiers.
     *
     * @param character       Physical character to refer to
     * @param foregroundColor Foreground color the character has
     * @param backgroundColor Background color the character has
     * @param modifiers       Set of modifiers to apply when drawing the character
     */
    private TextCharacter(
            char character,
            TextColor foregroundColor,
            TextColor backgroundColor,
            @NotNull EnumSet<TextEffect> modifiers) {
        this(Character.toString(character), foregroundColor, backgroundColor, modifiers);
    }

    /**
     * Creates a new {@link TextCharacter} based on a physical character,
     * color information and a set of modifiers.
     *
     * @param character       Physical character to refer to
     * @param foregroundColor Foreground color the character has
     * @param backgroundColor Background color the character has
     * @param modifiers       Set of modifiers to apply when drawing the character
     */
    private TextCharacter(
            String character,
            TextColor foregroundColor,
            TextColor backgroundColor,
            @NotNull TextEffect... modifiers) {
        this(character, foregroundColor, backgroundColor, toEnumSet(modifiers));
    }

    /**
     * Creates a new {@link TextCharacter} based on a physical character,
     * color information and a set of modifiers.
     *
     * @param character       Physical character to refer to
     * @param foregroundColor Foreground color the character has
     * @param backgroundColor Background color the character has
     * @param modifiers       Set of modifiers to apply when drawing the character
     */
    private TextCharacter(
            String character,
            TextColor foregroundColor,
            TextColor backgroundColor,
            @NotNull EnumSet<TextEffect> modifiers) {

        if (character.isEmpty()) {
            throw new IllegalArgumentException("Cannot create TextCharacter from an empty string");
        }
        validateSingleCharacter(character);

        // intern the string so we don't waste more memory than necessary
        this.character = character.intern();
        char firstCharacter = character.charAt(0);

        // Don't allow creating a TextCharacter containing a control character
        // For backwards-compatibility, do allow tab for now
        if (TerminalTextUtils.isControlCharacter(firstCharacter) && firstCharacter != '\t') {
            throw new IllegalArgumentException("Cannot create a TextCharacter from a control character (0x" + Integer.toHexString(firstCharacter) + ")");
        }

        if (foregroundColor == null) {
            foregroundColor = TextColor.ANSI.DEFAULT;
        }
        if (backgroundColor == null) {
            backgroundColor = TextColor.ANSI.DEFAULT;
        }

        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.modifiers = EnumSet.copyOf(modifiers);
    }

    // Utilities

    private void validateSingleCharacter(final @NotNull String character) {
        BreakIterator breakIterator = BreakIterator.getCharacterInstance();
        breakIterator.setText(character);
        String firstCharacter = null;
        for (int begin = 0, end; (end = breakIterator.next()) != BreakIterator.DONE; begin = breakIterator.current()) {
            if (firstCharacter == null) {
                firstCharacter = character.substring(begin, end);
            } else {
                throw new IllegalArgumentException(String.format(
                        "Invalid String '%s' for TextCharacter, can only have one logical character",
                        character
                ));
            }
        }
    }

    private static EnumSet<TextEffect> toEnumSet(final @NotNull TextEffect... modifiers) {
        if (modifiers.length == 0) {
            return EnumSet.noneOf(TextEffect.class);
        } else {
            return EnumSet.copyOf(Arrays.asList(modifiers));
        }
    }

    // Factory methods

    @NotNull
    public static TextCharacter fromCharacter(char character) {
        return new TextCharacter(character, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
    }

    @NotNull
    public static TextCharacter fromCharacter(char character, @NotNull TextEffect... modifiers) {
        return new TextCharacter(
                Character.toString(character),
                DEFAULT.foregroundColor,
                DEFAULT.backgroundColor,
                modifiers
        );
    }

    @NotNull
    public static TextCharacter fromCharacter(char character,
                                              TextColor foregroundColor,
                                              TextColor backgroundColor,
                                              @NotNull TextEffect... modifiers) {
        return new TextCharacter(
                Character.toString(character),
                foregroundColor,
                backgroundColor,
                modifiers
        );
    }

    @NotNull
    public static TextCharacter fromCharacter(char character,
                                              TextColor foregroundColor,
                                              TextColor backgroundColor,
                                              @NotNull EnumSet<TextEffect> modifiers) {
        return new TextCharacter(Character.toString(character), foregroundColor, backgroundColor, modifiers);
    }

    @NotNull
    public static TextCharacter[] fromString(@NotNull String string) {
        return fromString(string, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
    }

    @NotNull
    public static TextCharacter[] fromString(
            @NotNull String string,
            TextColor foregroundColor,
            TextColor backgroundColor,
            @NotNull TextEffect... modifiers) {
        return fromString(string, foregroundColor, backgroundColor, toEnumSet(modifiers));
    }

    @NotNull
    public static TextCharacter[] fromString(
            @NotNull String string,
            TextColor foregroundColor,
            TextColor backgroundColor,
            @NotNull EnumSet<@NotNull TextEffect> modifiers) {

        BreakIterator breakIterator = BreakIterator.getCharacterInstance();
        breakIterator.setText(string);
        List<TextCharacter> result = new ArrayList<>();
        for (int begin = 0, end; (end = breakIterator.next()) != BreakIterator.DONE; begin = breakIterator.current()) {
            result.add(new TextCharacter(string.substring(begin, end), foregroundColor, backgroundColor, modifiers));
        }
        return result.toArray(new TextCharacter[0]);
    }

    //
    // Getters
    //

    /**
     * Returns the character this TextCharacter represents as a String. This is not returning a char
     *
     * @return the character this TextCharacter represents as a String
     */
    @NotNull
    public String getCharacter() {
        return character;
    }

    /**
     * Foreground color specified for this TextCharacter
     *
     * @return Foreground color of this TextCharacter
     */
    @NotNull
    public TextColor getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Background color specified for this TextCharacter
     *
     * @return Background color of this TextCharacter
     */
    @NotNull
    public TextColor getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns a set of all active modifiers on this TextCharacter
     *
     * @return Set of active TextEffect codes
     */
    public @NotNull EnumSet<@NotNull TextEffect> getModifiers() {
        // Using #copyOf because EnumSets aren't immutable
        return EnumSet.copyOf(modifiers);
    }

    public boolean is(char otherCharacter) {
        return character.length() == 1 && otherCharacter == character.charAt(0);
    }

    /**
     * Returns true if this TextCharacter has the bold modifier active
     *
     * @return {@code true} if this TextCharacter has the bold modifier active
     */
    public boolean isBold() {
        return modifiers.contains(TextEffect.INTENSITY_BRIGHT_OR_BOLD);
    }

    /**
     * Returns true if this TextCharacter has the invert modifier active
     *
     * @return {@code true} if this TextCharacter has the invert modifier active
     */
    public boolean isReversed() {
        return modifiers.contains(TextEffect.INVERT);
    }

    /**
     * Returns true if this TextCharacter has the underline modifier active
     *
     * @return {@code true} if this TextCharacter has the underline modifier active
     */
    public boolean isUnderlined() {
        return modifiers.contains(TextEffect.UNDERLINE_SINGLE) || modifiers.contains(TextEffect.UNDERLINE_DOUBLE);
    }

    /**
     * Returns true if this TextCharacter has the blink modifier active
     *
     * @return {@code true} if this TextCharacter has the blink modifier active
     */
    public boolean isBlinking() {
        return modifiers.contains(TextEffect.BLINK_SLOW) || modifiers.contains(TextEffect.BLINK_FAST);
    }

    /**
     * Returns true if this TextCharacter has the crossed-out modifier active
     *
     * @return {@code true} if this TextCharacter has the crossed-out modifier active
     */
    public boolean isCrossedOut() {
        return modifiers.contains(TextEffect.STRIKE_THROUGH);
    }

    /**
     * Returns true if this TextCharacter has the italic modifier active
     *
     * @return {@code true} if this TextCharacter has the italic modifier active
     */
    public boolean isItalic() {
        return modifiers.contains(TextEffect.EMPHASIS_ITALIC);
    }

    /**
     * @return Whether this TextCharacter is double or single width
     * @deprecated Use #width() instead
     */
    @Deprecated
    public boolean isDoubleWidth() {
        // TODO: make this better to work properly with emoji and other complicated "characters"
        return TerminalTextUtils.isCharDoubleWidth(character.charAt(0)) ||
                // If the character takes up more than one char, assume it's double width (unless thai)
                (character.length() > 1 && !TerminalTextUtils.isCharThai(character.charAt(0)));
    }

    public int width() {
        return WCWidth.stringWidth(character);
    }

    //
    // Modification methods
    //

    /**
     * Returns a new TextCharacter with the same colors and modifiers but a different underlying character
     *
     * @param character Character the copy should have
     * @return Copy of this TextCharacter with different underlying character
     */
    public TextCharacter withCharacter(char character) {
        if (this.character.equals(Character.toString(character))) {
            return this;
        }
        return new TextCharacter(character, foregroundColor, backgroundColor, modifiers);
    }

    /**
     * Returns a copy of this TextCharacter with a specified foreground color
     *
     * @param foregroundColor Foreground color the copy should have
     * @return Copy of the TextCharacter with a different foreground color
     */
    public TextCharacter withForegroundColor(TextColor foregroundColor) {
        if (this.foregroundColor == foregroundColor || this.foregroundColor.equals(foregroundColor)) {
            return this;
        }
        return new TextCharacter(character, foregroundColor, backgroundColor, modifiers);
    }

    /**
     * Returns a copy of this TextCharacter with a specified background color
     *
     * @param backgroundColor Background color the copy should have
     * @return Copy of the TextCharacter with a different background color
     */
    public TextCharacter withBackgroundColor(TextColor backgroundColor) {
        if (this.backgroundColor == backgroundColor || this.backgroundColor.equals(backgroundColor)) {
            return this;
        }
        return new TextCharacter(character, foregroundColor, backgroundColor, modifiers);
    }

    /**
     * Returns a copy of this TextCharacter with specified list of TextEffect modifiers. None of the currently active TextEffect codes
     * will be carried over to the copy, only those in the passed in value.
     *
     * @param modifiers TextEffect modifiers the copy should have
     * @return Copy of the TextCharacter with a different set of TextEffect modifiers
     */
    public TextCharacter withModifiers(Collection<TextEffect> modifiers) {
        EnumSet<TextEffect> newSet = EnumSet.copyOf(modifiers);
        if (modifiers.equals(newSet)) {
            return this;
        }
        return new TextCharacter(character, foregroundColor, backgroundColor, newSet);
    }

    /**
     * Returns a copy of this TextCharacter with an additional TextEffect modifier. All of the currently active TextEffect codes
     * will be carried over to the copy, in addition to the one specified.
     *
     * @param modifier TextEffect modifiers the copy should have in additional to all currently present
     * @return Copy of the TextCharacter with a new TextEffect modifier
     */
    public TextCharacter withModifier(TextEffect modifier) {
        if (modifiers.contains(modifier)) {
            return this;
        }
        EnumSet<TextEffect> newSet = EnumSet.copyOf(this.modifiers);
        newSet.add(modifier);
        return new TextCharacter(character, foregroundColor, backgroundColor, newSet);
    }

    /**
     * Returns a copy of this TextCharacter with an TextEffect modifier removed. All of the currently active TextEffect codes
     * will be carried over to the copy, except for the one specified. If the current TextCharacter doesn't have the
     * TextEffect specified, it will return itself.
     *
     * @param modifier TextEffect modifiers the copy should not have
     * @return Copy of the TextCharacter without the TextEffect modifier
     */
    public TextCharacter withoutModifier(TextEffect modifier) {
        if (!modifiers.contains(modifier)) {
            return this;
        }
        EnumSet<TextEffect> newSet = EnumSet.copyOf(this.modifiers);
        newSet.remove(modifier);
        return new TextCharacter(character, foregroundColor, backgroundColor, newSet);
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextCharacter other = (TextCharacter) obj;
        if (!Objects.equals(this.character, other.character)) {
            return false;
        }
        if (!Objects.equals(this.foregroundColor, other.foregroundColor)) {
            return false;
        }
        if (!Objects.equals(this.backgroundColor, other.backgroundColor)) {
            return false;
        }
        return Objects.equals(this.modifiers, other.modifiers);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.character.hashCode();
        hash = 37 * hash + (this.foregroundColor != null ? this.foregroundColor.hashCode() : 0);
        hash = 37 * hash + (this.backgroundColor != null ? this.backgroundColor.hashCode() : 0);
        hash = 37 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "TextCharacter{" + "character=" + character + ", foregroundColor=" + foregroundColor + ", backgroundColor=" + backgroundColor + ", modifiers=" + modifiers + '}';
    }
}