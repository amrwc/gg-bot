package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that lists available games.
 */
@Component
public class GamesListener extends MessageListenerBase {

    private static final String KEYWORD = "games";

    private final DiscordMessageUtil messageUtil;

    public GamesListener(final DiscordMessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        final var embedBuilder = messageUtil.buildEmbedInfo(event, "Available Games")
                .setDescription("- Slot Machine (`slot`)");
        event.getChannel().sendMessage(embedBuilder);
    }
}
