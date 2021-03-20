package dev.amrw.ggbot.helper;

import dev.amrw.ggbot.dto.GameVerdict;
import dev.amrw.ggbot.dto.RoshamboRequest;
import dev.amrw.ggbot.dto.RoshamboResult;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import dev.amrw.ggbot.util.ResourceUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for {@link dev.amrw.ggbot.listener.RoshamboListener}.
 */
@Log4j2
@Component
public class RoshamboListenerHelper {

    private static final String RESULT_FIELD_FORMAT = "%s %s %s\n**-- YOU %s --**";
    private static final String LOADING_GIF_PATH = "/img/roshambo/loading.gif";
    private static final String LOADING_GIF_NAME = "roshambo-loading.gif";

    private final DiscordMessageUtil messageUtil;
    private final ResourceUtil resourceUtil;

    public RoshamboListenerHelper(final DiscordMessageUtil messageUtil, final ResourceUtil resourceUtil) {
        this.messageUtil = messageUtil;
        this.resourceUtil = resourceUtil;
    }

    /**
     * Displays a loading GIF, removes the message with the GIF, because
     * Steps performed:
     * <ol>
     *     <li>Builds a result message, catering for loss, or win/draw.</li>
     *     <li>Sends a message with a loading GIF.</li>
     *     <li>After minimum of 3 seconds, removes the loading GIF message.</li>
     *     <li>Sends the randomised shape in a new message.</li>
     *     <li>Sends the detailed result in a new message.</li>
     * </ol>
     * @param event current {@link MessageCreateEvent}
     * @param request {@link RoshamboRequest}
     * @param result {@link RoshamboResult}
     */
    public void displayResultSuspensefully(
            final MessageCreateEvent event,
            final RoshamboRequest request,
            final RoshamboResult result
    ) {
        final var resultField = String.format(RESULT_FIELD_FORMAT,
                request.getShape().getEmoji(),
                getOperatorForVerdict(result.getVerdict()),
                result.getShape().getEmoji(),
                result.getVerdict().getPastTense()
        );
        final var resultMessage = messageUtil.buildInfo(event, "Roshambo")
                .addField("Result", resultField)
                .addInlineField("Credits won", result.getCreditsWon().toString())
                .addInlineField("Net profit", result.getNetProfit().toString())
                .addInlineField("Current balance", result.getCurrentBalance().toString());

        sendLoadingMessage(event)
                .whenCompleteAsync((message, throwable) -> event.getChannel().sendMessage(result.getShape().getEmoji()))
                .whenCompleteAsync((message, throwable) -> event.getChannel().sendMessage(resultMessage));
    }

    /**
     * Returns an operator representing relationships between two Roshambo shapes.
     * <p>
     * NOTE: This method assumes that the requested shape is on the left, and the randomised result is on the right.
     * The given verdict comes from {@link RoshamboResult}.
     * @param verdict {@link GameVerdict}
     * @return result operator
     */
    private String getOperatorForVerdict(final GameVerdict verdict) {
        switch (verdict) {
            case WIN:
                return ">";
            case DRAW:
                return "==";
            case LOSS:
                return "<";
            default:
                return "";
        }
    }

    /**
     * Sends a loading message to the given channel, and removes it after a minimum of 3 seconds.
     * @param event {@link MessageCreateEvent}
     * @return {@link CompletableFuture} that involves deleting the loading message
     */
    private CompletableFuture<Message> sendLoadingMessage(final MessageCreateEvent event) {
        final var animation = resourceUtil.getResourceAsStream(RoshamboListenerHelper.class, LOADING_GIF_PATH);
        final var delayedExecutor = CompletableFuture.delayedExecutor(3L, TimeUnit.SECONDS);
        return event.getChannel().sendMessage(animation, LOADING_GIF_NAME)
                .whenCompleteAsync((message, throwable) -> {
                    try {
                        // The InputStream must only be closed _after_ the message has been sent
                        animation.close();
                    } catch (final IOException exception) {
                        log.error("Error closing InputStream", exception);
                    }
                })
                .whenCompleteAsync((message, throwable) -> message.delete(), delayedExecutor);
    }
}
