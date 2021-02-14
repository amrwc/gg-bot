package dev.amrw.ggbot.util;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscordMessageUtilTest {

    @Mock
    private MessageAuthor messageAuthor;
    @Mock
    private User user;

    @Test
    @DisplayName("Should have got mention tag from the given MessageAuthor object")
    void shouldHaveGotMentionTag() {
        final var mentionTag = randomAlphanumeric(16);
        when(messageAuthor.asUser()).thenReturn(Optional.of(user));
        when(user.getMentionTag()).thenReturn(mentionTag);
        assertThat(DiscordMessageUtil.getMentionTagOrDisplayName(messageAuthor)).isEqualTo(mentionTag);
    }

    @Test
    @DisplayName("Should have got display name when User object is unavailable")
    void shouldHaveGotDisplayName() {
        final var displayName = randomAlphanumeric(16);
        when(messageAuthor.asUser()).thenReturn(Optional.empty());
        when(messageAuthor.getDisplayName()).thenReturn(displayName);
        assertThat(DiscordMessageUtil.getMentionTagOrDisplayName(messageAuthor)).isEqualTo(displayName);
    }
}
