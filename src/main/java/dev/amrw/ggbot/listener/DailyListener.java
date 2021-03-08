package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.service.DailyService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that gives the user daily credits.
 */
@Log4j2
@Component
public class DailyListener extends MessageListenerBase {

    static final String KEYWORD = "daily";

    private final DailyService dailyService;
    private final DiscordMessageUtil messageUtil;

    public DailyListener(final DailyService dailyService, final DiscordMessageUtil messageUtil) {
        this.dailyService = dailyService;
        this.messageUtil = messageUtil;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        log.debug("Processing daily for {}", event.getMessageAuthor());
        final var dailyCreditsResult = dailyService.claimDailyCredits(event);
        final var userCredit = dailyCreditsResult.getUserCredit();
        final var embedBuilder = dailyCreditsResult.getError()
                .map(error -> messageUtil.buildError(event, error)
                        .addField("Next daily in", userCredit.getTimeLeftUntilNextDaily()))
                .orElseGet(() -> messageUtil.buildInfo(event, "Daily Credits")
                        .addField("New credits", dailyCreditsResult.getClaimedCredits().toString()));
        embedBuilder.addField("Current balance", userCredit.getCredits().toString());
        event.getChannel().sendMessage(embedBuilder);
    }
}
