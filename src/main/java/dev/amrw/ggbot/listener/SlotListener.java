package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.PlayRequest;
import dev.amrw.ggbot.helper.SlotListenerHelper;
import dev.amrw.ggbot.service.SlotService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Listener that enables the users to play slots.
 */
@Log4j2
@Component
public class SlotListener implements MessageCreateListener {

    static final String KEYWORD = "slot";

    private final BotConfig botConfig;
    private final SlotService service;
    private final DiscordMessageUtil messageUtil;
    private final SlotListenerHelper helper;

    public SlotListener(
            final BotConfig botConfig,
            final SlotService service,
            final DiscordMessageUtil messageUtil,
            final SlotListenerHelper helper
    ) {
        this.botConfig = botConfig;
        this.service = service;
        this.messageUtil = messageUtil;
        this.helper = helper;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        final var messageParts = messageContent.split("\\s+");
        if (needsHelp(messageParts)) {
            sendHelpMessage(event);
            return;
        }

        final var playRequest = parseBet(event, messageParts);
        if (playRequest.getBet() <= 0) {
            return;
        }

        final var slotResult = service.play(playRequest);
        if (!slotResult.hasPlayed()) {
            event.getChannel().sendMessage(messageUtil.buildErrorEmbed(
                    event.getMessageAuthor(),
                    (null == slotResult.getError() ? Error.UNKNOWN_ERROR : slotResult.getError()).getMessage()
            ));
            return;
        }

        helper.displayResultSuspensefully(event, slotResult);
    }

    /**
     * Determines whether the send message has to be sent in response to a user's recent message. The reasons are:
     * <ul>
     *     <li>too few arguments,</li>
     *     <li><code>help</code> keyword is among the arguments.</li>
     * </ul>
     * @param message user's message split by white space
     * @return whether the user needs to see the help message
     */
    private boolean needsHelp(final String[] message) {
        return 2 == message.length || Set.of(message).contains("help");
    }

    private void sendHelpMessage(final MessageCreateEvent event) {
        final var embedBuilder = new EmbedBuilder()
                .setColor(botConfig.getEmbedColour())
                .setTitle("Slot Machine")
                .addField("Rules", "🥇🥇❔ – **0.5x**\n" +
                        "💎💎❔ – **2x**\n" +
                        "💯💯❔ – **2x**\n" +
                        "🥇🥇🥇 – **2.5x**\n" +
                        "💎💎💎 – **3x**\n" +
                        "💵💵❔ – **3.5x**\n" +
                        "💯💯💯 – **4x**\n" +
                        "💰💰❔ – **7x**\n" +
                        "💵💵💵 – **7x**\n" +
                        "💰💰💰 – **15x**")
                .addField("Usage", "`" + botConfig.getTrigger() + " slot <bet_amount>`");
        event.getChannel().sendMessage(embedBuilder);
    }

    private PlayRequest parseBet(final MessageCreateEvent event, final String[] messageParts) {
        final var betString = messageParts[2];
        final var messageAuthor = event.getMessageAuthor();
        final var playRequest = new PlayRequest();
        playRequest.setMessageAuthor(messageAuthor);
        var invalidBet = false;

        try {
            playRequest.setBet(Long.parseLong(betString));
        } catch (final NumberFormatException exception) {
            log.debug(
                    "User '{} ({})' placed an invalid bet: '{}'",
                    messageAuthor.getDisplayName(),
                    messageAuthor.getId(),
                    betString,
                    exception
            );
            invalidBet = true;
        }

        if (invalidBet) {
            event.getChannel().sendMessage(messageUtil.buildErrorEmbed(messageAuthor, String.format(
                    "'%s' is an invalid bet. You can view the instructions with `%s slot help`",
                    betString,
                    botConfig.getTrigger())));
        } else if (playRequest.getBet() <= 0L) {
            event.getChannel().sendMessage(
                    messageUtil.buildErrorEmbed(messageAuthor, Error.NEGATIVE_BET.getMessage()));
        }

        return playRequest;
    }
}
