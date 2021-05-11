package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.GameRequest;
import dev.amrw.ggbot.helper.SlotListenerHelper;
import dev.amrw.ggbot.service.SlotService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Listener that enables the users to play slots.
 */
@Log4j2
@Component
public class SlotListener extends MessageListenerBase {

    static final String KEYWORD = "slot";

    private final SlotService service;
    private final DiscordMessageUtil messageUtil;
    private final SlotListenerHelper helper;

    public SlotListener(
            final SlotService service,
            final DiscordMessageUtil messageUtil,
            final SlotListenerHelper helper
    ) {
        this.service = service;
        this.messageUtil = messageUtil;
        this.helper = helper;
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    public void process(final MessageCreateEvent event) {
        log.debug("Playing Slots with {}", event.getMessageAuthor());
        final var playRequest = parseCommand(event);
        if (playRequest.getBet() <= 0) {
            return;
        }

        final var slotResult = service.play(playRequest);
        if (!slotResult.hasPlayed()) {
            event.getChannel().sendMessage(messageUtil.buildError(
                    event,
                    (slotResult.getError().isEmpty() ? Error.UNKNOWN_ERROR : slotResult.getError().get()).getMessage()
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
     * @param event {@link MessageCreateEvent}
     * @return whether the user needs to see the help message
     */
    @Override
    public boolean needsHelp(final MessageCreateEvent event) {
        final var messageParts = event.getMessageContent().toLowerCase().split("\\s+");
        return messageParts.length <= 2 || Set.of(messageParts).contains("help");
    }

    @Override
    public void showHelp(final MessageCreateEvent event) {
        final var helpMessage = messageUtil.buildInfo(event, "Slot Machine")
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
                .addField("Usage", "`" + getPrefix() + " <bet_amount>`");
        event.getChannel().sendMessage(helpMessage);
    }

    private GameRequest parseCommand(final MessageCreateEvent event) {
        final var messageParts = event.getMessageContent().toLowerCase().split("\\s+");
        final var slotRequest = new GameRequest();
        slotRequest.setEvent(event);

        try {
            slotRequest.setBet(Long.parseLong(messageParts[2]));
        } catch (final NumberFormatException exception) {
            log.debug("{} used an invalid command: {}", event.getMessageAuthor(), event.getMessageContent(), exception);
            event.getChannel().sendMessage(messageUtil.buildInvalidCommandError(event, getPrefix()));
            slotRequest.setError(Error.INVALID_COMMAND);
            return slotRequest;
        }

        if (slotRequest.getBet() <= 0L) {
            log.debug("{} placed a negative bet: {}", event.getMessageAuthor(), event.getMessageContent());
            event.getChannel().sendMessage(messageUtil.buildError(event, Error.NEGATIVE_BET));
            slotRequest.setError(Error.NEGATIVE_BET);
        }

        return slotRequest;
    }
}
