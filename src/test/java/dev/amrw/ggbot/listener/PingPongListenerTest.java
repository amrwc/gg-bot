package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PingPongListenerTest {

    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private PingPongListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    @Mock
    private TextChannel channel;
    @Mock
    private EmbedBuilder embedBuilder;

    @Test
    @DisplayName("Should have handled a message with correct trigger and pattern")
    void shouldHaveHandledMessage() {
        when(event.getMessage()).thenReturn(message);
        when(event.getChannel()).thenReturn(channel);
        when(messageUtil.buildInfo(event, "Ping?")).thenReturn(embedBuilder);
        when(embedBuilder.setDescription("pong!")).thenReturn(embedBuilder);

        listener.process(event);

        verify(message).addReaction("🏓");
        verify(channel).sendMessage(embedBuilder);
    }
}
