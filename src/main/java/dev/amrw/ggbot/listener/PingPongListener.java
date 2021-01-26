package dev.amrw.ggbot.listener;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

/**
 * Listener that replies with 'pong' to 'ping'.
 */
public class PingPongListener implements MessageCreateListener {

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        if (!event.getMessage().getContent().equalsIgnoreCase("!gg ping")) {
            return;
        }
        event.getMessage().addReaction("🏓");
        event.getChannel().sendMessage("pong!");
    }
}
