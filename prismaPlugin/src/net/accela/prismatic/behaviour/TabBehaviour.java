package net.accela.prismatic.behaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TabBehaviour {
    /**
     * Tab characters are not replaced. This will have undefined and weird behaviour.
     */
    IGNORE(null, null),

    /**
     * Tab characters are replaced with a single blank space, no matter where the tab was placed.
     */
    CONVERT_TO_ONE_SPACE(1, null),

    /**
     * Tab characters are replaced with two blank spaces, no matter where the tab was placed.
     */
    CONVERT_TO_TWO_SPACES(2, null),

    /**
     * Tab characters are replaced with three blank spaces, no matter where the tab was placed.
     */
    CONVERT_TO_THREE_SPACES(3, null),

    /**
     * Tab characters are replaced with four blank spaces, no matter where the tab was placed.
     */
    CONVERT_TO_FOUR_SPACES(4, null),

    /**
     * Tab characters are replaced with eight blank spaces, no matter where the tab was placed.
     */
    CONVERT_TO_EIGHT_SPACES(8, null),

    /**
     * Tab characters are replaced with enough space characters to reach the next column index that is evenly divisible
     * by 4, simulating a normal tab character when placed inside a text document.
     */
    ALIGN_TO_COLUMN_4(null, 4),

    /**
     * Tab characters are replaced with enough space characters to reach the next column index that is evenly divisible
     * by 8, simulating a normal tab character when placed inside a text document.
     */
    ALIGN_TO_COLUMN_8(null, 8);

    private final @Nullable Integer replaceFactor;
    private final @Nullable Integer alignFactor;

    TabBehaviour(@Nullable Integer replaceFactor, @Nullable Integer alignFactor) {
        this.replaceFactor = replaceFactor;
        this.alignFactor = alignFactor;
    }

    /**
     * Given a string, being placed on the screen at column X, returns the same string with all tab characters (\t)
     * replaced according to this TabBehaviour.
     *
     * @param string      String that is going to be put to the screen, potentially containing tab characters
     * @param columnIndex Column on the screen where the first character of the string is going to end up
     * @return The input string with all tab characters replaced with spaces, according to this TabBehaviour
     */
    public @NotNull String replaceTabs(@NotNull String string, int columnIndex) {
        int tabPosition = string.indexOf('\t');
        while (tabPosition != -1) {
            String tabReplacementHere = getTabReplacement(columnIndex + tabPosition);
            string = string.substring(0, tabPosition) + tabReplacementHere + string.substring(tabPosition + 1);
            tabPosition += tabReplacementHere.length();
            tabPosition = string.indexOf('\t', tabPosition);
        }
        return string;
    }

    /**
     * Returns the String that can replace a tab at the specified position, according to this TabBehaviour.
     *
     * @param columnIndex Column index of where the tab character is placed
     * @return String consisting of 1 or more space character
     */
    public @NotNull String getTabReplacement(int columnIndex) {
        int replaceCount;
        StringBuilder replace = new StringBuilder();
        if (replaceFactor != null) {
            replaceCount = replaceFactor;
        } else if (alignFactor != null) {
            replaceCount = alignFactor - (columnIndex % alignFactor);
        } else {
            return "\t";
        }
        replace.append(" ".repeat(Math.max(0, replaceCount)));
        return replace.toString();
    }
}