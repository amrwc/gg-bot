package dev.amrw.ggbot.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Result of a Roshambo (Rock, Paper, Scissors) game.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class RoshamboResult extends GameResult {

    private RoshamboShape shape;
}
