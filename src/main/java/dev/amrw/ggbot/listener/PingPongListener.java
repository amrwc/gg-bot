package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

/**
 * Listener that replies with 'pong' to 'ping'.
 */
@Component
public class PingPongListener implements MessageCreateListener {

    static final String KEYWORD = "ping";

    private final BotConfig botConfig;

    public PingPongListener(final BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        event.getMessage().addReaction("🏓");
        final var embedBuilder = new EmbedBuilder()
                .setColor(botConfig.getEmbedColour())
                .setDescription("pong!");
        event.getChannel().sendMessage(embedBuilder);
    }
}
