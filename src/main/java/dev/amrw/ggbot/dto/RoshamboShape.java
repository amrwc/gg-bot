package dev.amrw.ggbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents available shapes in Roshambo.
 */
@Getter
@ToString
@AllArgsConstructor
public enum RoshamboShape {

    ROCK("🤘"),
    PAPER("🧻"),
    SCISSORS("✂️");

    private final String emoji;
}
