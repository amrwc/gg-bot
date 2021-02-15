package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.util.DiscordMessageUtil;
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
class PingPongListenerTest {

    @Mock
    private BotConfig botConfig;
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

    private String prefix;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + PingPongListener.KEYWORD;
        when(event.getMessage()).thenReturn(message);
        when(botConfig.getTrigger()).thenReturn(trigger);
    }

    @Test
    @DisplayName("Should not have handled a message with wrong prefix")
    void shouldNotHaveHandledMessageWithWrongPrefix() {
        when(message.getContent()).thenReturn(randomAlphanumeric(16));

        listener.onMessageCreate(event);

        verifyNoMoreInteractions(event, message);
    }

    @Test
    @DisplayName("Should have handled a message with correct trigger and pattern")
    void shouldHaveHandledMessage() {
        when(message.getContent()).thenReturn(prefix);
        when(event.getChannel()).thenReturn(channel);
        when(messageUtil.buildEmbedInfo(event, "Ping?")).thenReturn(embedBuilder);
        when(embedBuilder.setDescription("pong!")).thenReturn(embedBuilder);

        listener.onMessageCreate(event);

        verify(message).addReaction("🏓");
        verify(channel).sendMessage(embedBuilder);
    }
}
