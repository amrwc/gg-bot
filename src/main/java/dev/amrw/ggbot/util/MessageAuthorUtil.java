package dev.amrw.ggbot.util;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.user.User;
import org.springframework.stereotype.Component;

/**
 * Utility class for handling {@link MessageAuthor}.
 */
@Component
public class MessageAuthorUtil {

    /**
     * Attempts to retrieve Discord mention tag of the {@link User} of the given {@link MessageAuthor}, and falls back
     * to display name if the {@link User} object is unavailable.
     * @param messageAuthor {@link MessageAuthor}
     * @return mention tag or display name
     */
    public static String getMentionTagOrDisplayName(final MessageAuthor messageAuthor) {
        return messageAuthor.asUser()
                .map(User::getMentionTag)
                .orElseGet(messageAuthor::getDisplayName);
    }
}
