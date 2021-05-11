package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that lists available games.
 */
@Log4j2
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
        log.debug("Listing available games for {}", event.getMessageAuthor());
        final var embedBuilder = messageUtil.buildInfo(event, "Available Games")
                .setDescription("- Slot Machine (`slot`)");
        event.getChannel().sendMessage(embedBuilder);
    }
}
