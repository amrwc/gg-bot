package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.resource.BotConfig;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

/**
 * Listener that replies with 'pong' to 'ping'.
 */
@Component
public class PingPongListener implements MessageCreateListener {

    private final BotConfig botConfig;

    public PingPongListener(final BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        if (!event.getMessage().getContent().equalsIgnoreCase(botConfig.getTrigger() + " ping")) {
            return;
        }
        event.getMessage().addReaction("🏓");
        event.getChannel().sendMessage("pong!");
    }
}
