package dev.amrw.ggbot.util;

import dev.amrw.ggbot.config.BotConfig;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.springframework.stereotype.Component;

import java.awt.Color;

/**
 * Utility class for handling {@link MessageAuthor}.
 */
@Component
public class DiscordMessageUtil {

    private final BotConfig botConfig;

    public DiscordMessageUtil(final BotConfig botConfig) {
        this.botConfig = botConfig;
    }

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

    /**
     * Prepares an info {@link EmbedBuilder}.
     * @param messageAuthor Discord user the info relates to
     * @param title embed title
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildInfoEmbed(final MessageAuthor messageAuthor, final String title) {
        return new EmbedBuilder()
                .setColor(botConfig.getEmbedColour())
                .setAuthor(messageAuthor)
                .setTitle(title);
    }

    /**
     * Prepares an error {@link EmbedBuilder}.
     * @param messageAuthor Discord user the error relates to
     * @param description embed description; error message
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildErrorEmbed(final MessageAuthor messageAuthor, final String description) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(messageAuthor)
                .setTitle("Error")
                .setDescription(description);
    }
}
