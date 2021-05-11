package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.RoshamboRequest;
import dev.amrw.ggbot.dto.RoshamboShape;
import dev.amrw.ggbot.service.RoshamboService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Listener that enables users to play Roshambo (Rock, Paper, Scissors).
 */
@Log4j2
@Component
public class RoshamboListener extends MessageListenerBase {

    static final String KEYWORD = "roshambo";

    private final RoshamboService service;
    private final DiscordMessageUtil messageUtil;

    public RoshamboListener(final RoshamboService service, final DiscordMessageUtil messageUtil) {
        this.service = service;
        this.messageUtil = messageUtil;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        log.debug("Playing Roshambo with {}", event.getMessageAuthor());
        final var request = parseCommand(event);
        if (request.getError().isPresent()) {
            return;
        }

        final var result = service.play(request);

        final var delayedExecutor = CompletableFuture.delayedExecutor(1L, TimeUnit.SECONDS);
        var future = event.getChannel().sendMessage("3️⃣");
        for (final String content : new String[] {"2️⃣", "1️⃣", result.getShape().getEmoji()}) {
            future = future.whenCompleteAsync((message, throwable) -> message.edit(content), delayedExecutor);
        }

        final var resultMessage = messageUtil.buildInfo(event, "Roshambo")
                .addField("Result", String.format("**-- YOU %s --**", result.getVerdict().getPastTense()))
                .addInlineField("Credits won", result.getCreditsWon().toString())
                .addInlineField("Net profit", result.getNetProfit().toString())
                .addInlineField("Current balance", result.getCurrentBalance().toString());
        future.whenCompleteAsync((msg, thr) -> event.getChannel().sendMessage(resultMessage));
    }

    @Override
    public boolean needsHelp(final MessageCreateEvent event) {
        final var messageParts = event.getMessageContent().toLowerCase().split("\\s+");
        return messageParts.length < 4 || Set.of(messageParts).contains("help");
    }

    @Override
    public void showHelp(final MessageCreateEvent event) {
        final var helpMessage = messageUtil.buildInfo(event, "Roshambo")
                .addField("Rules", "🤘 > ✂️\n✂️ > 🧻\n🧻 > 🤘\nThe player stands to win 2:1.")
                .addField("Usage", "`" + getPrefix() + " <bet_amount> <shape>`");
        event.getChannel().sendMessage(helpMessage);
    }

    private RoshamboRequest parseCommand(final MessageCreateEvent event) {
        final var messageParts = event.getMessageContent().toLowerCase().split("\\s+");
        final var roshamboRequest = new RoshamboRequest();
        roshamboRequest.setEvent(event);

        try {
            roshamboRequest.setBet(Long.parseLong(messageParts[2]));
            roshamboRequest.setShape(RoshamboShape.valueOf(messageParts[3].toUpperCase()));
        } catch (final Exception exception) {
            log.debug("{} used an invalid command: {}", event.getMessageAuthor(), event.getMessageContent(), exception);
            event.getChannel().sendMessage(messageUtil.buildInvalidCommandError(event, getPrefix()));
            roshamboRequest.setError(Error.INVALID_COMMAND);
            return roshamboRequest;
        }

        if (roshamboRequest.getBet() <= 0L) {
            log.debug("{} placed a negative bet: {}", event.getMessageAuthor(), event.getMessageContent());
            event.getChannel().sendMessage(messageUtil.buildError(event, Error.NEGATIVE_BET));
            roshamboRequest.setError(Error.NEGATIVE_BET);
        }

        return roshamboRequest;
    }
}
