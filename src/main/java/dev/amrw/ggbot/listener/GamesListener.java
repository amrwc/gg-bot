package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

/**
 * Listener that lists available games.
 */
@Component
public class GamesListener implements MessageCreateListener {

    static final String KEYWORD = "games";

    private final BotConfig botConfig;
    private final DiscordMessageUtil messageUtil;

    public GamesListener(final BotConfig botConfig, final DiscordMessageUtil messageUtil) {
        this.botConfig = botConfig;
        this.messageUtil = messageUtil;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        final var embedBuilder = messageUtil.buildEmbedInfo(event, "Available Games")
                .setDescription("- Slot Machine (`slot`)");
        event.getChannel().sendMessage(embedBuilder);
    }
}
