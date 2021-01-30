package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.SlotResult;
import dev.amrw.ggbot.resource.BotConfig;
import dev.amrw.ggbot.service.SlotService;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotListenerTest {

    @Mock
    private SlotService service;
    @Mock
    private BotConfig botConfig;
    @InjectMocks
    private SlotListener listener;

    private String prefix;
    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    @Mock
    private TextChannel channel;
    @Mock
    private SlotResult slotResult;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + SlotListener.KEYWORD;
        when(botConfig.getTrigger()).thenReturn(trigger);
    }

    @Test
    @DisplayName("Should not have handled a message with wrong prefix")
    void shouldNotHaveHandledMessageWithWrongPrefix() {
        when(event.getMessage()).thenReturn(message);
        when(message.getContent()).thenReturn(randomAlphanumeric(16));

        listener.onMessageCreate(event);

        verify(message).getContent();
        verifyNoMoreInteractions(event, message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " help"})
    @DisplayName("Should have sent help message")
    void shouldHaveSentHelpMessage(final String messageContent) {
        when(event.getMessage()).thenReturn(message);
        when(message.getContent()).thenReturn(prefix + messageContent);
        when(event.getChannel()).thenReturn(channel);

        listener.onMessageCreate(event);

        verify(channel).sendMessage(any(EmbedBuilder.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-123", "abcd", "💯💯💯"})
    @DisplayName("Should have played a game of slots and displayed the result")
    void shouldNotHavePlayedWithInvalidBet(final String bet) {
        final var author = mock(MessageAuthor.class);
        final var user = mock(User.class);
        final var mentionTag = randomAlphanumeric(16);
        when(event.getMessage()).thenReturn(message);
        when(message.getAuthor()).thenReturn(author);
        when(author.asUser()).thenReturn(Optional.of(user));
        when(user.getMentionTag()).thenReturn(mentionTag);
        when(message.getContent()).thenReturn(prefix + " " + bet);
        when(event.getChannel()).thenReturn(channel);

        listener.onMessageCreate(event);

        verifyNoInteractions(service);
        final var stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(channel).sendMessage(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).contains(mentionTag).contains("invalid bet");
    }

    @Test
    @DisplayName("Should have played a game of slots and displayed the result")
    void shouldHavePlayedAndDisplayedResult() {
        final var bet = 100L;
        when(event.getMessage()).thenReturn(message);
        when(message.getContent()).thenReturn(prefix + " " + bet);
        when(service.play(bet)).thenReturn(slotResult);
        when(slotResult.getPayline()).thenReturn("💯💯💯");
        when(event.getChannel()).thenReturn(channel);

        listener.onMessageCreate(event);

        verify(channel).sendMessage(any(EmbedBuilder.class));
    }
}
