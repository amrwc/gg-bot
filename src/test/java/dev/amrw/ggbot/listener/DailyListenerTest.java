package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.service.DailyService;
import dev.amrw.ggbot.service.UserCreditsService;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyListenerTest {

    @Mock
    private DailyService dailyService;
    @Mock
    private UserCreditsService userCreditsService;
    @Mock
    private BotConfig botConfig;
    @InjectMocks
    private DailyListener listener;

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
    private TextChannel channel;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + DailyListener.KEYWORD;
        when(event.getMessage()).thenReturn(message);
        when(botConfig.getTrigger()).thenReturn(trigger);
    }

    @Test
    @DisplayName("Should not have handled a message with wrong prefix")
    void shouldNotHaveHandledMessageWithWrongPrefix() {
        when(message.getContent()).thenReturn(randomAlphanumeric(16));
        when(botConfig.getTrigger()).thenReturn("");
        listener.onMessageCreate(event);
        verifyNoMoreInteractions(event, message);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 2_500L})
    @DisplayName("Should have claimed daily credits and notified the channel")
    void shouldHaveClaimedDailyCreditsAndNotifiedChannel(final long claimedCredits) {
        final var credits = nextLong();
        when(message.getContent()).thenReturn(prefix);
        when(message.getAuthor()).thenReturn(messageAuthor);
        when(dailyService.claimDailyCredits(messageAuthor)).thenReturn(claimedCredits);
        when(userCreditsService.getOrCreateUserCredit(messageAuthor)).thenReturn(userCredit);
        when(userCredit.getCredits()).thenReturn(credits);
        when(event.getChannel()).thenReturn(channel);

        listener.onMessageCreate(event);

        // `EmbedBuilder` doesn't override `equals()`, so it's not easy to compare the actual contents
        verify(channel).sendMessage(any(EmbedBuilder.class));
    }
}
