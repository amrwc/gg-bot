package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.service.UserCreditsService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

/**
 * Listener that displays the user's credit balance.
 */
@Component
public class BalanceListener implements MessageCreateListener {

    static final String KEYWORD = "balance";

    private final BotConfig botConfig;
    private final UserCreditsService userCreditsService;
    private final DiscordMessageUtil messageUtil;

    public BalanceListener(
            final BotConfig botConfig,
            final UserCreditsService userCreditsService,
            final DiscordMessageUtil messageUtil
    ) {
        this.botConfig = botConfig;
        this.userCreditsService = userCreditsService;
        this.messageUtil = messageUtil;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        final var messageAuthor = event.getMessage().getAuthor();
        final var userCredit = userCreditsService.getOrCreateUserCredit(messageAuthor);
        final var embedBuilder = messageUtil.buildEmbedInfo(messageAuthor, "Credits Balance")
                .setDescription(userCredit.getCredits().toString());
        event.getChannel().sendMessage(embedBuilder);
    }
}
