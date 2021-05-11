package dev.amrw.ggbot.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EmojiUtilTest {

    @ParameterizedTest
    @CsvSource({
            "'🥇🥇❔', 1, '🥇'",
            "'🥇🥇❔', 2, '❔'",
            "'💎❔💎', 1, '❔'",
            "'💎❔💎', 2, '💎'",
            "'❌🎧🙈', 0, '❌'",
            "'❌🎧🙈', 1, '🎧'",
            "'❌🎧🙈', 2, '🙈'",
            "'💰🙈💰🙈💰🙈💰🙈💰🙈💰❌💰🙈💰🙈💰🙈💰🙈💰🙈💰', 11, '❌'",
    })
    void shouldHaveGotEmojiAtCodePoint(final String text, final int index, final String expectedResult) {
        assertThat(EmojiUtil.getEmojiAtCodePoint(text, index)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "'🥇🥇❔', 0, '🥇🥇❔'",
            "'🥇🥇❔', 1, '🥇❔'",
            "'🥇🥇❔', 2, '❔'",
            "'💎❔💎', 0, '💎❔💎'",
            "'💎❔💎', 1, '❔💎'",
            "'💎❔💎', 2, '💎'",
            "'❌🎧🙈', 2, '🙈'",
            "'💰🙈💰🙈💰🙈💰🙈💰🙈💰❌💰🙈💰🙈💰🙈💰🙈💰🙈💰', 11, '❌💰🙈💰🙈💰🙈💰🙈💰🙈💰'",
    })
    void shouldHaveGotEmojiSubstring(final String text, final int start, final String expectedResult) {
        assertThat(EmojiUtil.getEmojiSubstring(text, start)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "'🥇🥇❔', 0, 2, '🥇🥇'",
            "'🥇🥇❔', 1, 2, '🥇'",
            "'🥇🥇❔', 2, 2, ''",
            "'🥇🥇❔', 0, 1, '🥇'",
            "'❌🎧🙈', 0, 1, '❌'",
            "'💰🙈💰🙈💰🙈💰🙈💰🙈💰❌💰🙈💰🙈💰🙈💰🙈💰🙈💰', 11, 15, '❌💰🙈💰'",
    })
    void shouldHaveGotEmojiSubstring(final String text, final int start, final int end, final String expectedResult) {
        assertThat(EmojiUtil.getEmojiSubstring(text, start, end)).isEqualTo(expectedResult);
    }
}
