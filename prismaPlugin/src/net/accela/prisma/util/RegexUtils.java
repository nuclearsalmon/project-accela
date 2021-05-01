package net.accela.prisma.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    /**
     * Includes any pattern matches from the string and excludes everything else
     */
    public static String filterIncludeByPattern(@NotNull String string, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(string);
        StringBuilder sb = new StringBuilder(string);
        while (matcher.find()) {
            sb.append(matcher.group());
        }
        return sb.toString();
    }

    /**
     * Excludes all pattern matches and includes everything else
     */
    public static String filterExcludeByPattern(@NotNull String string, @NotNull Pattern pattern) {
        return string.replaceAll(pattern.toString(), "");
    }

    /**
     * Returns true if the pattern got at least one match
     */
    public static boolean testForMatch(@NotNull String string, @NotNull Pattern pattern) {
        return pattern.matcher(string).results().count() > 0;
    }
}
