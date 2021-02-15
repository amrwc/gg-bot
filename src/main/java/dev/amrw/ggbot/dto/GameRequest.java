package dev.amrw.ggbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javacord.api.entity.message.MessageAuthor;

/**
 * Play request for an arbitrary game.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequest {

    private long bet;
    private MessageAuthor messageAuthor;
}
