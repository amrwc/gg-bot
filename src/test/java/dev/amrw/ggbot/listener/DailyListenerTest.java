package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.dto.DailyCreditsResult;
import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.service.DailyService;
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
class DailyListenerTest {

    @Mock
    private DailyService dailyService;
    @Mock
    private BotConfig botConfig;
    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private DailyListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    @Mock
    private MessageAuthor messageAuthor;
    @Mock
    private UserCredit userCredit;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private TextChannel channel;

    private String prefix;
    private Long credits;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + DailyListener.KEYWORD;
        credits = nextLong();
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

    @Test
    @DisplayName("Should not have claimed daily credits and notified channel")
    void shouldNotHaveClaimedDailyCreditsAndNotifiedChannel() {
        final var error = Error.ALREADY_COLLECTED_DAILY;
        final var dailyCreditsResult = new DailyCreditsResult(0L, userCredit, error);
        final var nextDailyIn = randomAlphanumeric(16);

        when(messageUtil.buildEmbedError(event, error)).thenReturn(embedBuilder);
        when(userCredit.getTimeLeftUntilNextDaily()).thenReturn(nextDailyIn);
        when(embedBuilder.addField("Next daily in", nextDailyIn)).thenReturn(embedBuilder);
        addCommonStubbings(dailyCreditsResult);

        listener.onMessageCreate(event);

        verify(embedBuilder).addField("Current balance", credits.toString());
        verify(channel).sendMessage(embedBuilder);
    }

    @Test
    @DisplayName("Should have claimed daily credits and notified channel")
    void shouldHaveClaimedDailyCreditsAndNotifiedChannel() {
        final Long claimedCredits = nextLong();
        final var dailyCreditsResult = new DailyCreditsResult(claimedCredits, userCredit, null);

        when(messageUtil.buildEmbedInfo(event, "Daily Credits")).thenReturn(embedBuilder);
        when(embedBuilder.addField("New credits", claimedCredits.toString())).thenReturn(embedBuilder);
        addCommonStubbings(dailyCreditsResult);

        listener.onMessageCreate(event);

        verify(embedBuilder).addField("Current balance", credits.toString());
        verify(channel).sendMessage(embedBuilder);
    }

    private void addCommonStubbings(final DailyCreditsResult dailyCreditsResult) {
        when(message.getContent()).thenReturn(prefix);
        when(event.getMessageAuthor()).thenReturn(messageAuthor);
        when(dailyService.claimDailyCredits(messageAuthor)).thenReturn(dailyCreditsResult);

        when(userCredit.getCredits()).thenReturn(credits);
        when(event.getChannel()).thenReturn(channel);
    }
}
