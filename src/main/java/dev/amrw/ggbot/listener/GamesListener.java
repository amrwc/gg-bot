package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.resource.BotConfig;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Listener that lists available games.
 */
@Component
public class GamesListener implements MessageCreateListener {

    static final String KEYWORD = "games";

    private BotConfig botConfig;

    private GamesListener() {
        // noop
    }

    @Autowired(required = false)
    public GamesListener(final BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        final var embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Currently available games")
                .setDescription("- Slot Machine (`slot`)");
        event.getChannel().sendMessage(embedBuilder);
    }
}
