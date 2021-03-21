package dev.amrw.ggbot.dto;

import lombok.*;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Optional;

/**
 * Game request for an arbitrary game.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequest {

    private long bet;
    private MessageCreateEvent event;
    @Getter(AccessLevel.NONE)
    private Error error = null;

    public Optional<Error> getError() {
        return Optional.ofNullable(error);
    }
}
