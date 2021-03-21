package dev.amrw.ggbot.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Request for a Roshambo game.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RoshamboRequest extends GameRequest {

    private RoshamboShape shape;
}
