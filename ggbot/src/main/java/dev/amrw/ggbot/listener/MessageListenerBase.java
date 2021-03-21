package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Listener base, injecting common dependencies.
 */
@Log4j2
public abstract class MessageListenerBase implements MessageListener {

    @Autowired
    private BotConfig botConfig;
    @Autowired
    private DiscordMessageUtil messageUtil;

    @Override
    public String getPrefix() {
        return botConfig.getTrigger() + " " + getKeyword();
    }

    @Override
    public void showHelp(final MessageCreateEvent event) {
        log.debug("Showing help to {}", event.getMessageAuthor());
        final var helpMessage = messageUtil.buildInfo(event, "Help")
                .setDescription("Use `" + botConfig.getTrigger() + " help`");
        event.getChannel().sendMessage(helpMessage);
    }
}
