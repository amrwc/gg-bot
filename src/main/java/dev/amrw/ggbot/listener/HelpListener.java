package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listener for displaying the help message.
 */
@Component
public class HelpListener extends MessageListenerBase {

    private static final String KEYWORD = "help";

    private final DiscordMessageUtil messageUtil;
    private final List<MessageListener> listeners;

    public HelpListener(final DiscordMessageUtil messageUtil, final List<MessageListener> listeners) {
        this.messageUtil = messageUtil;
        this.listeners = listeners;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        final var helpMessage = messageUtil.buildInfo(event, "Help")
                .addField("Available keywords", getAvailableKeywords());
        event.getChannel().sendMessage(helpMessage);
    }

    private String getAvailableKeywords() {
        final var sb = new StringBuilder();
        sb.append("- `").append(getKeyword()).append("`");
        for (final var listener : listeners) {
            sb.append("\n- `").append(listener.getKeyword()).append("`");
        }
        return sb.toString();
    }
}
