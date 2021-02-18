package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageListenerBaseTest {

    @Mock
    private BotConfig botConfig;
    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private Listener listener;

    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private MessageCreateEvent event;
    @Mock
    private TextChannel channel;

    @Test
    @DisplayName("Should have got prefix from BotConfig")
    void shouldHaveGotPrefix() {
        when(botConfig.getTrigger()).thenReturn("!gg");
        assertThat(listener.getPrefix()).isEqualTo("!gg listener");
    }

    @Test
    @DisplayName("Should have showed help to the user")
    void shouldHaveShowedHelp() {
        when(messageUtil.buildInfo(event, "Help")).thenReturn(embedBuilder);
        when(botConfig.getTrigger()).thenReturn("!gg");
        when(embedBuilder.setDescription("Use `!gg help`")).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);

        listener.showHelp(event);

        verify(channel).sendMessage(embedBuilder);
    }

    static class Listener extends MessageListenerBase {

        private static final String KEYWORD = "listener";

        @Override
        public String getKeyword() {
            return KEYWORD;
        }

        @Override
        public void process(final MessageCreateEvent event) {
        }
    }
}
