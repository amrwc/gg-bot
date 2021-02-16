package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.service.UserCreditsService;
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

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceListenerTest {

    @Mock
    private UserCreditsService userCreditsService;
    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private BalanceListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private UserCredit userCredit;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private TextChannel channel;

    @Test
    @DisplayName("Should have displayed the user's credits balance")
    void shouldHaveDisplayedCreditsBalance() {
        final Long credits = nextLong();
        when(event.getChannel()).thenReturn(channel);
        when(userCreditsService.getOrCreateUserCredit(event)).thenReturn(userCredit);
        when(messageUtil.buildEmbedInfo(event, "Credits Balance")).thenReturn(embedBuilder);
        when(userCredit.getCredits()).thenReturn(credits);
        when(embedBuilder.setDescription(credits.toString())).thenReturn(embedBuilder);

        listener.process(event);

        verify(channel).sendMessage(embedBuilder);
        verifyNoMoreInteractions(userCreditsService, messageUtil, channel);
    }
}
