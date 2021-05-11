package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.DailyCreditsResult;
import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.service.DailyService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyListenerTest {

    @Mock
    private DailyService dailyService;
    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private DailyListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private UserCredit userCredit;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private TextChannel channel;

    private Long credits;

    @BeforeEach
    void beforeEach() {
        credits = nextLong();
    }

    @Test
    @DisplayName("Should not have claimed daily credits and notified channel")
    void shouldNotHaveClaimedDailyCreditsAndNotifiedChannel() {
        final var error = Error.ALREADY_COLLECTED_DAILY;
        final var dailyCreditsResult = new DailyCreditsResult(0L, userCredit, error);
        final var nextDailyIn = randomAlphanumeric(16);

        when(dailyService.claimDailyCredits(event)).thenReturn(dailyCreditsResult);

        when(messageUtil.buildError(event, error)).thenReturn(embedBuilder);
        when(userCredit.getTimeLeftUntilNextDaily()).thenReturn(nextDailyIn);
        when(embedBuilder.addField("Next daily in", nextDailyIn)).thenReturn(embedBuilder);

        when(userCredit.getCredits()).thenReturn(credits);
        when(event.getChannel()).thenReturn(channel);

        listener.process(event);

        verify(embedBuilder).addField("Current balance", credits.toString());
        verify(channel).sendMessage(embedBuilder);
    }

    @Test
    @DisplayName("Should have claimed daily credits and notified channel")
    void shouldHaveClaimedDailyCreditsAndNotifiedChannel() {
        final Long claimedCredits = nextLong();
        final var dailyCreditsResult = new DailyCreditsResult(claimedCredits, userCredit, null);

        when(dailyService.claimDailyCredits(event)).thenReturn(dailyCreditsResult);

        when(messageUtil.buildInfo(event, "Daily Credits")).thenReturn(embedBuilder);
        when(embedBuilder.addField("New credits", claimedCredits.toString())).thenReturn(embedBuilder);

        when(userCredit.getCredits()).thenReturn(credits);
        when(event.getChannel()).thenReturn(channel);

        listener.process(event);

        verify(embedBuilder).addField("Current balance", credits.toString());
        verify(channel).sendMessage(embedBuilder);
    }
}
