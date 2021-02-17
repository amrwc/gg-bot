package dev.amrw.ggbot.listener;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

/**
 * Interface marking an abstract message listener, and declaring/defining common logic.
 */
public interface MessageListener extends MessageCreateListener {

    /** @return the listener's keyword; the word after the bot's trigger */
    String getKeyword();

    /** @return the listener's prefix; the bot's trigger and the listener's keyword separated by white space */
    String getPrefix();

    /**
     * Processes the given {@link MessageCreateEvent}.
     * @param event {@link MessageCreateEvent}
     */
    void process(final MessageCreateEvent event);

    /**
     * Determines whether the given message has a prefix matching the listener.
     * @param event {@link MessageCreateEvent}
     * @return whether the prefix matches
     */
    default boolean hasMatchingPrefix(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = getPrefix().toLowerCase();
        return messageContent.startsWith(prefix);
    }

    @Override
    default void onMessageCreate(final MessageCreateEvent event) {
        if (hasMatchingPrefix(event)) {
            process(event);
        }
    }
}
