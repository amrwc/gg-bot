package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.RoshamboRequest;
import dev.amrw.ggbot.dto.RoshamboShape;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Listener that enables users to play Roshambo (Rock, Paper, Scissors).
 */
@Log4j2
@Component
public class RoshamboListener extends MessageListenerBase {

    private static final String KEYWORD = "roshambo";

    private final DiscordMessageUtil messageUtil;

    public RoshamboListener(final DiscordMessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        final var gameRequest = parseBet(event);
        log.info(gameRequest);
    }

    @Override
    public boolean needsHelp(final MessageCreateEvent event) {
        final var messageParts = event.getMessage().getContent().toLowerCase().split("\\s+");
        return messageParts.length < 4 || Set.of(messageParts).contains("help");
    }

    @Override
    public void showHelp(final MessageCreateEvent event) {
        final var helpMessage = messageUtil.buildEmbedInfo(event, "Roshambo")
                .addField("Rules", "🤘 > ✂️\n✂️ > 🧻\n🧻 > 🤘\nThe player stands to win 2:1.")
                .addField("Usage", "`" + getPrefix() + " <bet_amount> <shape>");
        event.getChannel().sendMessage(helpMessage);
    }

    private RoshamboRequest parseBet(final MessageCreateEvent event) {
        final var messageParts = event.getMessage().getContent().toLowerCase().split("\\s+");
        final var betString = messageParts[2];
        final var shapeString = messageParts[3];
        final var messageAuthor = event.getMessageAuthor();
        final var gameRequest = new RoshamboRequest();
        var invalidBet = false;

        try {
            gameRequest.setBet(Long.parseLong(betString));
            gameRequest.setShape(RoshamboShape.valueOf(shapeString.toUpperCase()));
        } catch (final Exception exception) {
            log.debug(
                    "User '{} ({})' placed an invalid bet: '{} {}'",
                    messageAuthor.getDisplayName(),
                    messageAuthor.getId(),
                    betString,
                    shapeString,
                    exception
            );
            invalidBet = true;
        }

        if (invalidBet) {
            event.getChannel().sendMessage(messageUtil.buildEmbedError(event, String.format(
                    "'%s' is an invalid bet. You can view the instructions with `%s help`", betString, getPrefix())));
        } else if (gameRequest.getBet() <= 0L) {
            event.getChannel().sendMessage(messageUtil.buildEmbedError(event, Error.NEGATIVE_BET.getMessage()));
        }

        return gameRequest;
    }
}
