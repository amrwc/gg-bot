package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Listener base, injecting common dependencies.
 */
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
        final var helpMessage = messageUtil.buildEmbedInfo(event, "Help")
                .setDescription("Use `" + botConfig.getTrigger() + " help`");
        event.getChannel().sendMessage(helpMessage);
    }
}
