package dev.amrw.ggbot.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request for a Roshambo game.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoshamboRequest extends GameRequest {

    private RoshamboShape shape;
}
