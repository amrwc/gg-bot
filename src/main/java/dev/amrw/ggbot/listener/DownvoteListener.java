package dev.amrw.ggbot.listener;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Listener that removes a message after it's been downvoted.
 */
@Component
public class DownvoteListener implements MessageCreateListener {

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        event.getMessage().addReactionAddListener(reactionAddEvent -> {
            if (reactionAddEvent.getEmoji().equalsEmoji("👎")) {
                reactionAddEvent.deleteMessage();
            }
        }).removeAfter(5, TimeUnit.MINUTES);
    }
}
