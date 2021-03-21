package dev.amrw.ggbot.util;

/**
 * Utility class for working with emojis.
 */
public class EmojiUtil {

    /**
     * Retrieves an emoji from the given text at the given code point index.
     * <p>
     * Creating this method was motivated by the varying lengths of emoji characters in strings. E.g.
     * <code>"🥇🥇❔".length() == 5</code>, but <code>"🥇🥇🥇".length() == 6</code>. Therefore, to get an emoji at a
     * certain offset, the program would have to know character-length of each preceding character.
     * @param text input
     * @param index index of the emoji code point
     * @return the emoji
     */
    public static String getEmojiAtCodePoint(final String text, final int index) {
        return new String(text.codePoints().toArray(), index, 1);
    }

    /**
     * Retrieves a substring from the given text from the given start index.
     * @param text input
     * @param start start inclusive
     * @return substring
     */
    public static String getEmojiSubstring(final String text, final int start) {
        final var codePoints = text.codePoints().toArray();
        return new String(codePoints, start, codePoints.length - start);
    }

    /**
     * Retrieves a substring from the given text at the given range.
     * @param text input
     * @param start start inclusive
     * @param end end exclusive
     * @return substring
     */
    public static String getEmojiSubstring(final String text, final int start, final int end) {
        return new String(text.codePoints().toArray(), start, end - start);
    }
}
