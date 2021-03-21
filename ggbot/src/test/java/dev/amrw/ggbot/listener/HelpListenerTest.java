package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelpListenerTest {

    @Mock
    private DiscordMessageUtil messageUtil;
    @Spy
    private ArrayList<MessageListener> listeners;
    @InjectMocks
    private HelpListener listener;

    @Mock
    private MessageListener mockListener;
    @Mock
    private MessageCreateEvent event;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private TextChannel channel;

    @BeforeEach
    void beforeEach() {
        listeners.add(mockListener);
    }

    @Test
    void shouldHaveDisplayedHelpMessage() {
        final var keyword = randomAlphanumeric(16);
        when(messageUtil.buildInfo(event, "Help")).thenReturn(embedBuilder);
        when(mockListener.getKeyword()).thenReturn(keyword);
        when(embedBuilder.addField(eq("Available keywords"), anyString())).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);

        listener.process(event);

        final var stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(embedBuilder).addField(eq("Available keywords"), stringCaptor.capture());
        assertThat(stringCaptor.getValue()).contains(keyword);
        verify(channel).sendMessage(embedBuilder);
    }
}
