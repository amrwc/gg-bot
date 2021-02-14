package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.service.UserCreditsService;
import dev.amrw.ggbot.util.MessageAuthorUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
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

    public BalanceListener(final BotConfig botConfig, final UserCreditsService userCreditsService) {
        this.botConfig = botConfig;
        this.userCreditsService = userCreditsService;
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
        final var embedBuilder = new EmbedBuilder()
                .setColor(botConfig.getEmbedColour())
                .setTitle("Credit Balance")
                .addField("User", MessageAuthorUtil.getMentionTagOrDisplayName(messageAuthor))
                .addField("Current balance", "" + userCredit.getCredits());
        event.getChannel().sendMessage(embedBuilder);
    }
}
