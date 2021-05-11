package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.service.UserCreditsService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that displays the user's credit balance.
 */
@Log4j2
@Component
public class BalanceListener extends MessageListenerBase {

    static final String KEYWORD = "balance";

    private final UserCreditsService userCreditsService;
    private final DiscordMessageUtil messageUtil;

    public BalanceListener(final UserCreditsService userCreditsService, final DiscordMessageUtil messageUtil) {
        this.userCreditsService = userCreditsService;
        this.messageUtil = messageUtil;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        log.debug("Fetching balance for {}", event.getMessageAuthor());
        final var userCredit = userCreditsService.getOrCreateUserCredit(event);
        final var embedBuilder = messageUtil.buildInfo(event, "Credits Balance")
                .setDescription(userCredit.getCredits().toString());
        event.getChannel().sendMessage(embedBuilder);
    }
}
