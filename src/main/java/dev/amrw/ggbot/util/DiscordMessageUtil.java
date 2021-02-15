package dev.amrw.ggbot.util;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.dto.Error;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
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
     * Prepares an info {@link EmbedBuilder}.
     * @param messageAuthor Discord user the info relates to
     * @param title embed title
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildEmbedInfo(final MessageCreateEvent event, final String title) {
        return new EmbedBuilder()
                .setColor(botConfig.getEmbedColour())
                .setAuthor(event.getMessageAuthor())
                .setTitle(title);
    }

    /**
     * Prepares an error {@link EmbedBuilder}.
     * @param messageAuthor Discord user the error relates to
     * @param error {@link Error} to use as an embed description/error message
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildEmbedError(final MessageCreateEvent event, final Error error) {
        return buildEmbedError(event, error.getMessage());
    }

    /**
     * Prepares an error {@link EmbedBuilder}.
     * @param messageAuthor Discord user the error relates to
     * @param description embed description; error message
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildEmbedError(final MessageCreateEvent event, final String description) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(event.getMessageAuthor())
                .setTitle("Error")
                .setDescription(description);
    }
}
