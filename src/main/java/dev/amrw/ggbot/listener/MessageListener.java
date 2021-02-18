package dev.amrw.ggbot.listener;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Set;

/**
 * Interface marking an abstract message listener, and declaring/defining common logic.
 */
public interface MessageListener extends MessageCreateListener {

    /** @return the listener's keyword; the word after the bot's trigger */
    String getKeyword();

    /** @return the listener's prefix; the bot's trigger and the listener's keyword separated by white space */
    String getPrefix();

    /**
     * Sends a help message about the given listener.
     * @param event {@link MessageCreateEvent}
     */
    void showHelp(final MessageCreateEvent event);

    /**
     * Processes the given {@link MessageCreateEvent}.
     * @param event {@link MessageCreateEvent}
     */
    void process(final MessageCreateEvent event);

    /**
     * Main method called by the Javacord library in every {@link MessageListener}.
     * @param event {@link MessageCreateEvent}
     */
    @Override
    default void onMessageCreate(final MessageCreateEvent event) {
        if (hasMatchingPrefix(event)) {
            if (needsHelp(event)) {
                showHelp(event);
            } else {
                process(event);
            }
        }
    }

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

    /**
     * Determines whether the user asked for, or needs to see help message of the given listener.
     * <p>
     * Note that the user may request the help message with the <code>help</code> keyword after the given listener's
     * keyword, or if the keyword matches, but the remaining required arguments are missing, or wrong.
     * @param event {@link MessageCreateEvent}
     * @return whether to show the user a help message
     */
    default boolean needsHelp(final MessageCreateEvent event) {
        final var messageParts = event.getMessage().getContent().split("\\s+");
        if (messageParts.length >= 2 && "help".equals(messageParts[1])) {
            // `HelpListener` should take over and display a comprehensive help message
            return false;
        }
        return messageParts.length < 2 || Set.of(messageParts).contains("help");
    }
}
