package dev.amrw.ggbot.helper;

import dev.amrw.ggbot.dto.SlotResult;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import dev.amrw.ggbot.util.EmojiUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for {@link dev.amrw.ggbot.listener.SlotListener}.
 */
@Component
public class SlotListenerHelper {

    private static final String PAYLINE_FORMAT = "**------------------**\n**| %s | %s | %s |**\n**------------------**";
    private static final String OUTCOME_MESSAGE_FORMAT = "**-- YOU %s --**";

    private final DiscordMessageUtil messageUtil;

    public SlotListenerHelper(final DiscordMessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    /**
     * Displays each next column of the payline with a slight delay, with the final result showed after the last
     * column has been revealed.
     * @param event current {@link MessageCreateEvent}
     * @param result {@link SlotResult}
     */
    public void displayResultSuspensefully(final MessageCreateEvent event, final SlotResult result) {
        final String column1 = EmojiUtil.getEmojiAtCodePoint(result.getPayline(), 0);
        final String column2 = EmojiUtil.getEmojiAtCodePoint(result.getPayline(), 1);
        final String column3 = EmojiUtil.getEmojiAtCodePoint(result.getPayline(), 2);
        var future = event.getChannel().sendMessage(buildResultEmbed(event, "❔", "❔", "❔"));
        final var edits = new EmbedBuilder[] {
                buildResultEmbed(event, column1, "❔", "❔"),
                buildResultEmbed(event, column1, column2, "❔"),
                buildResultEmbed(event, column1, column2, column3)
                        .updateFields(
                                field -> "Result".equals(field.getName()),
                                field -> field.setValue(field.getValue() + "\n" + getOutcomeMessage(result)))
                        .addField("Net profit", result.getNetProfit().toString()),
        };
        for (final EmbedBuilder edit : edits) {
            future = future.whenCompleteAsync(
                    (message, throwable) -> message.edit(edit),
                    CompletableFuture.delayedExecutor(1L, TimeUnit.SECONDS)
            );
        }
    }

    private EmbedBuilder buildResultEmbed(
            final MessageCreateEvent event,
            final String column1,
            final String column2,
            final String column3
    ) {
        return messageUtil.buildInfoEmbed(event.getMessageAuthor(), "Slot Machine")
                .addField("Result", String.format(PAYLINE_FORMAT, column1, column2, column3));
    }

    private String getOutcomeMessage(final SlotResult result) {
        return String.format(OUTCOME_MESSAGE_FORMAT, result.getCreditsWon() > 0L ? "WON" : "LOST");
    }
}
