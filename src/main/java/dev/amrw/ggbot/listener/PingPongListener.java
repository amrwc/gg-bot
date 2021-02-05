package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.resource.BotConfig;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listener that replies with 'pong' to 'ping'.
 */
@Component
public class PingPongListener implements MessageCreateListener {

    static final String KEYWORD = "ping";

    private BotConfig botConfig;

    private PingPongListener() {
        // noop
    }

    @Autowired(required = false)
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
        event.getChannel().sendMessage("pong!");
    }
}
