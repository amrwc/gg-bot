package dev.amrw.ggbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javacord.api.event.message.MessageCreateEvent;

/**
 * Game request for an arbitrary game.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequest {

    private long bet;
    private MessageCreateEvent event;
}
