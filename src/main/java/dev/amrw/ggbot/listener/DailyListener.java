package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.service.DailyService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

/**
 * Listener that gives the user daily credits.
 */
@Component
public class DailyListener implements MessageCreateListener {

    static final String KEYWORD = "daily";

    private final BotConfig botConfig;
    private final DailyService dailyService;
    private final DiscordMessageUtil messageUtil;

    public DailyListener(
            final BotConfig botConfig,
            final DailyService dailyService,
            final DiscordMessageUtil messageUtil
    ) {
        this.botConfig = botConfig;
        this.dailyService = dailyService;
        this.messageUtil = messageUtil;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        final var messageAuthor = event.getMessageAuthor();
        final var dailyCreditsResult = dailyService.claimDailyCredits(messageAuthor);
        final var userCredit = dailyCreditsResult.getUserCredit();
        final var embedBuilder = dailyCreditsResult.getError()
                .map(error -> messageUtil.buildEmbedError(messageAuthor, error)
                        .addField("Next daily in", userCredit.getTimeLeftUntilNextDaily()))
                .orElseGet(() -> messageUtil.buildEmbedInfo(messageAuthor, "Daily Credits")
                        .addField("New credits", dailyCreditsResult.getClaimedCredits().toString()));
        embedBuilder.addField("Current balance", userCredit.getCredits().toString());
        event.getChannel().sendMessage(embedBuilder);
    }
}
