package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.resource.BotConfig;
import dev.amrw.ggbot.service.UserCreditsService;
import dev.amrw.ggbot.util.MessageAuthorUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Listener that displays the user's credit balance.
 */
@Component
public class BalanceListener implements MessageCreateListener {

    static final String KEYWORD = "balance";

    private final UserCreditsService userCreditsService;
    private BotConfig botConfig;

    @Autowired(required = false)
    private BalanceListener(final UserCreditsService userCreditsService) {
        this.userCreditsService = userCreditsService;
    }

    @Autowired(required = false)
    public BalanceListener(final UserCreditsService userCreditsService, final BotConfig botConfig) {
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
        final var userCredit = userCreditsService.getOrCreateUserCredit(messageAuthor);
        final var embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Credit Balance")
                .addField("User", MessageAuthorUtil.getMentionTagOrDisplayName(messageAuthor))
                .addField("Current balance", "" + userCredit.getCredits());
        event.getChannel().sendMessage(embedBuilder);
    }
}
