package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.util.DiscordMessageUtil;
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
    private final DiscordMessageUtil messageUtil;

    public PingPongListener(final BotConfig botConfig, final DiscordMessageUtil messageUtil) {
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

        event.getMessage().addReaction("🏓");
        event.getChannel().sendMessage(messageUtil.buildEmbedInfo(event, "Ping?").setDescription("pong!"));
    }
}
