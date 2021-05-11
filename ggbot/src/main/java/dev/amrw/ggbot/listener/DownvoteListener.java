package dev.amrw.ggbot.listener;

import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Listener that removes a message after it's been downvoted.
 */
@Log4j2
@Component
public class DownvoteListener implements MessageCreateListener {

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        log.debug("Removing message downvoted by {}", event.getMessageAuthor());
        event.getMessage().addReactionAddListener(reactionAddEvent -> {
            if (reactionAddEvent.getEmoji().equalsEmoji("👎")) {
                reactionAddEvent.deleteMessage();
            }
        }).removeAfter(5, TimeUnit.MINUTES);
    }
}
