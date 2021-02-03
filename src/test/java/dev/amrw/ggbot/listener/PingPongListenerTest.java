package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.resource.BotConfig;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
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
class PingPongListenerTest {

    @Mock
    private BotConfig botConfig;
    @InjectMocks
    private PingPongListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    @Mock
    private TextChannel channel;

    @Test
    @DisplayName("Should not have handled a message with wrong prefix")
    void shouldNotHaveHandledMessageWithWrongPrefix() {
        when(event.getMessage()).thenReturn(message);
        when(message.getContent()).thenReturn(randomAlphanumeric(16));
        when(botConfig.getTrigger()).thenReturn("");

        listener.onMessageCreate(event);

        verifyNoMoreInteractions(event, message);
    }

    @Test
    @DisplayName("Should have handled a message with correct trigger and pattern")
    void shouldHaveHandledMessage() {
        final var trigger = randomAlphabetic(3);
        when(event.getMessage()).thenReturn(message);
        when(message.getContent()).thenReturn(trigger + " ping");
        when(botConfig.getTrigger()).thenReturn(trigger);
        when(event.getChannel()).thenReturn(channel);

        listener.onMessageCreate(event);

        verify(message).getContent();
        verify(message).addReaction("🏓");
        verify(event).getChannel();
        verify(channel).sendMessage("pong!");
    }
}
