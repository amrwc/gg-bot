package dev.amrw.ggbot.listener;

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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamesListenerTest {

    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private GamesListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private TextChannel channel;
    @Mock
    private EmbedBuilder embedBuilder;

    @Test
    @DisplayName("Should have displayed currently available games")
    void shouldHaveDisplayedAvailableGames() {
        when(event.getChannel()).thenReturn(channel);
        when(messageUtil.buildInfo(event, "Available Games")).thenReturn(embedBuilder);
        when(embedBuilder.setDescription(anyString())).thenReturn(embedBuilder);

        listener.process(event);

        verify(channel).sendMessage(embedBuilder);
    }
}
