package dev.amrw.ggbot.helper;

import dev.amrw.ggbot.dto.SlotResult;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for {@link dev.amrw.ggbot.listener.SlotListener}.
 */
@Component
public class SlotListenerHelper {

    /**
     * Displays each next column of the payline with a slight delay, with the final result showed after the last
     * column has been revealed.
     * @param event current {@link MessageCreateEvent}
     * @param result {@link SlotResult}
     */
    public void displayResultSuspensefully(final MessageCreateEvent event, final SlotResult result) {
        final var resultFormat = "**------------------**\n**| %s | %s | %s |**\n**------------------**";
        final var columns = new String[] {
                result.getPayline().substring(0, 2),
                result.getPayline().substring(2, 4),
                result.getPayline().substring(4)
        };
        var future = event.getChannel().sendMessage(getEmbedBuilder(String.format(resultFormat, "❔", "❔", "❔")));
        final var edits = new EmbedBuilder[] {
                getEmbedBuilder(String.format(resultFormat, columns[0], "❔", "❔")),
                getEmbedBuilder(String.format(resultFormat, columns[0], columns[1], "❔")),
                getEmbedBuilder(String.format(resultFormat + "\n**-- YOU %s --**",
                        columns[0], columns[1], columns[2], result.getCreditsWon() > 0L ? "WON" : "LOST"))
                        .addField("Credits won", String.valueOf(result.getCreditsWon()))
        };
        for (final EmbedBuilder edit : edits) {
            future = future.whenCompleteAsync(
                    (message, throwable) -> message.edit(edit),
                    CompletableFuture.delayedExecutor(1L, TimeUnit.SECONDS)
            );
        }
    }

    private EmbedBuilder getEmbedBuilder(final String resultContent) {
        return new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Slot Machine")
                .addField("Result", resultContent);
    }
}
