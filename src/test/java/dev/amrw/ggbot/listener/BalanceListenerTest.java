package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.service.UserCreditsService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
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
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceListenerTest {

    @Mock
    private UserCreditsService userCreditsService;
    @Mock
    private BotConfig botConfig;
    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private BalanceListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    private String prefix;
    @Mock
    private MessageAuthor messageAuthor;
    @Mock
    private UserCredit userCredit;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private TextChannel channel;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + BalanceListener.KEYWORD;
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
    @DisplayName("Should have displayed the user's credits balance")
    void shouldHaveDisplayedCreditsBalance() {
        final Long credits = nextLong();
        when(message.getContent()).thenReturn(prefix);
        when(event.getChannel()).thenReturn(channel);
        when(message.getAuthor()).thenReturn(messageAuthor);
        when(userCreditsService.getOrCreateUserCredit(messageAuthor)).thenReturn(userCredit);
        when(messageUtil.buildEmbedInfo(messageAuthor, "Credits Balance")).thenReturn(embedBuilder);
        when(userCredit.getCredits()).thenReturn(credits);
        when(embedBuilder.setDescription(credits.toString())).thenReturn(embedBuilder);

        listener.onMessageCreate(event);

        verify(channel).sendMessage(embedBuilder);
        verifyNoMoreInteractions(userCreditsService, messageUtil, channel);
    }
}
