package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Listener base, injecting common dependencies.
 */
public abstract class MessageListenerBase implements MessageListener {

    @Autowired
    private BotConfig botConfig;

    @Override
    public String getPrefix() {
        return botConfig.getTrigger() + " " + getKeyword();
    }
}
