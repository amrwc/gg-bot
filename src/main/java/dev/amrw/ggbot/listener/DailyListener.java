package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.resource.BotConfig;
import dev.amrw.ggbot.service.DailyService;
import dev.amrw.ggbot.service.UserCreditsService;
import dev.amrw.ggbot.util.MessageAuthorUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Listener that gives the user daily credits.
 */
@Component
public class DailyListener implements MessageCreateListener {

    static final String KEYWORD = "daily";

    private final DailyService dailyService;
    private final UserCreditsService userCreditsService;
    private BotConfig botConfig;

    @Autowired(required = false)
    private DailyListener(final DailyService dailyService, final UserCreditsService userCreditsService) {
        this.dailyService = dailyService;
        this.userCreditsService = userCreditsService;
    }

    @Autowired(required = false)
    public DailyListener(
            final DailyService dailyService,
            final UserCreditsService userCreditsService,
            final BotConfig botConfig
    ) {
        this.dailyService = dailyService;
        this.userCreditsService = userCreditsService;
        this.botConfig = botConfig;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        final var messageAuthor = event.getMessage().getAuthor();
        final var embedBuilder = new EmbedBuilder()
                .setTitle("Daily Credits")
                .addField("User", MessageAuthorUtil.getMentionTagOrDisplayName(messageAuthor));
        final var claimedCredits = dailyService.claimDailyCredits(messageAuthor);
        final var userCredit = userCreditsService.getOrCreateUserCredit(messageAuthor);
        if (claimedCredits > 0L) {
            embedBuilder
                    .setColor(Color.ORANGE)
                    .addField("New credits", "" + claimedCredits);
        } else {
            embedBuilder
                    .setColor(Color.RED)
                    .addField("Error", "_You have already collected your daily credits today_")
                    .addField("Next daily in", userCredit.getTimeLeftUntilNextDaily());
        }
        embedBuilder.addField("Current balance", "" + userCredit.getCredits());
        event.getChannel().sendMessage(embedBuilder);
    }
}
