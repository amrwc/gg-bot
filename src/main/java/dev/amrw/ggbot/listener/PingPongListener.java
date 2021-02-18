package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that replies with 'pong!' to 'ping'.
 */
@Component
public class PingPongListener extends MessageListenerBase {

    static final String KEYWORD = "ping";

    private final DiscordMessageUtil messageUtil;

    public PingPongListener(final DiscordMessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        event.getMessage().addReaction("🏓");
        event.getChannel().sendMessage(messageUtil.buildInfo(event, "Ping?").setDescription("pong!"));
    }
}
