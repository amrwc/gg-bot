package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.resource.BotConfig;
import dev.amrw.ggbot.service.UserCreditsService;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listener that gives the user daily credits.
 */
@Component
public class DailyListener implements MessageCreateListener {

    static final String KEYWORD = "daily";

    private final UserCreditsService userCreditsService;
    private BotConfig botConfig;

    @Autowired(required = false)
    private DailyListener(final UserCreditsService userCreditsService) {
        this.userCreditsService = userCreditsService;
    }

    @Autowired(required = false)
    public DailyListener(final UserCreditsService userCreditsService, final BotConfig botConfig) {
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

        final var claimedDailyCredits = userCreditsService.claimDailyCredits(event.getMessage().getAuthor(), 800L);
        if (claimedDailyCredits) {
            event.getChannel().sendMessage("Added credit");
        } else {
            event.getChannel().sendMessage("Didn't add credit");
        }
    }
}
