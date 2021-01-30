package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.helper.SlotListenerHelper;
import dev.amrw.ggbot.resource.BotConfig;
import dev.amrw.ggbot.service.SlotService;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;

/**
 * Listener that enables the users to play slots.
 */
@Log4j2
@Component
public class SlotListener implements MessageCreateListener {

    static final String KEYWORD = "slot";

    private final SlotService service;
    private final SlotListenerHelper helper;
    private BotConfig botConfig;

    @Autowired(required = false)
    private SlotListener(final SlotService service, final SlotListenerHelper helper) {
        this.service = service;
        this.helper = helper;
    }

    @Autowired(required = false)
    public SlotListener(final SlotService service, final SlotListenerHelper helper, final BotConfig botConfig) {
        this.service = service;
        this.helper = helper;
        this.botConfig = botConfig;
    }

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        final var messageContent = event.getMessage().getContent().toLowerCase();
        final var prefix = (botConfig.getTrigger() + " " + KEYWORD).toLowerCase();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        final var messageParts = messageContent.split(" ");
        if (needsHelp(messageParts)) {
            sendHelpMessage(event);
            return;
        }

        final var bet = parseBet(event, messageParts);
        if (bet < 0) {
            return;
        }

        helper.displayResultSuspensefully(event, service.play(bet));
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
        return 2 == message.length || Arrays.asList(message).contains("help");
    }

    private void sendHelpMessage(final MessageCreateEvent event) {
        final var embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
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

    private long parseBet(final MessageCreateEvent event, final String[] messageParts) {
        final var betString = messageParts[2];
        var bet = 0L;
        try {
            bet = Long.parseLong(betString);
        } catch (final NumberFormatException exception) {
            log.debug(
                    "User '{} ({})' placed an invalid bet: '{}'",
                    event.getMessage().getAuthor().getDisplayName(),
                    event.getMessage().getAuthor().getId(),
                    betString
            );
            bet = -1L;
        }
        if (bet < 0) {
            event.getChannel().sendMessage(String.format(
                    "%s'%s' is an invalid bet. You can view the instructions with `%s slot help`",
                    event.getMessage().getAuthor().asUser().map(u -> u.getMentionTag() + ", ").orElse(""),
                    betString,
                    botConfig.getTrigger()
            ));
        }
        return bet;
    }
}
