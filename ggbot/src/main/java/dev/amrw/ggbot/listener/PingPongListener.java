package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that replies with 'pong!' to 'ping'.
 */
@Log4j2
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
        log.debug("Playing ping-pong with {}", event.getMessageAuthor());
        event.getMessage().addReaction("🏓");
        event.getChannel().sendMessage(messageUtil.buildInfo(event, "Ping?").setDescription("pong!"));
    }
}
