package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamesListenerTest {

    @Mock
    private BotConfig botConfig;
    @InjectMocks
    private GamesListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    private String prefix;
    @Mock
    private TextChannel channel;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + GamesListener.KEYWORD;
        when(event.getMessage()).thenReturn(message);
        when(botConfig.getTrigger()).thenReturn(trigger);
    }

    @Test
    @DisplayName("Should not have handled a message with wrong prefix")
    void shouldNotHaveHandledMessageWithWrongPrefix() {
        when(message.getContent()).thenReturn(randomAlphanumeric(16));
        listener.onMessageCreate(event);
        verifyNoMoreInteractions(event, message);
        verifyNoInteractions(channel);
    }

    @Test
    @DisplayName("Should have displayed currently available games")
    void shouldHaveDisplayedAvailableGames() {
        when(message.getContent()).thenReturn(prefix);
        when(event.getChannel()).thenReturn(channel);
        listener.onMessageCreate(event);
        verify(channel).sendMessage(any(EmbedBuilder.class));
        verifyNoMoreInteractions(event, message, channel);
    }
}
