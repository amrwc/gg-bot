package dev.amrw.ggbot.util;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.dto.Error;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.awt.Color;

/**
 * Utility class for handling and building Discord messages.
 */
@Component
public class DiscordMessageUtil {

    private final BotConfig botConfig;

    public DiscordMessageUtil(final BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    /**
     * Prepares an info {@link EmbedBuilder}.
     * @param event Discord event the info relates to
     * @param title embed title
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildInfo(final MessageCreateEvent event, final String title) {
        return new EmbedBuilder()
                .setColor(botConfig.getEmbedColour())
                .setAuthor(event.getMessageAuthor())
                .setTitle(title);
    }

    /**
     * Prepares an error {@link EmbedBuilder}.
     * @param event Discord event the error relates to
     * @param error {@link Error} to use as an embed description/error message
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildError(final MessageCreateEvent event, final Error error) {
        return buildError(event, error.getMessage());
    }

    /**
     * Builds an error message relating to an invalid command.
     * @param event {@link MessageCreateEvent}
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildInvalidCommandError(final MessageCreateEvent event, final String prefix) {
        final var description = String.format(
                "Invalid command: %s\nYou can view the instructions with `%s help`",
                event.getMessageContent(),
                prefix
        );
        return buildError(event, description);
    }

    /**
     * Prepares an error {@link EmbedBuilder}.
     * @param event Discord event the error relates to
     * @param description embed description; error message
     * @return {@link EmbedBuilder} ready to be sent to the Discord channel
     */
    public EmbedBuilder buildError(final MessageCreateEvent event, final String description) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(event.getMessageAuthor())
                .setTitle("Error")
                .setDescription(description);
    }
}
