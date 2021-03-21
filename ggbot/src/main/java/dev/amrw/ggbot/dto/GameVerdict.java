package dev.amrw.ggbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a verdict of an arbitrary game.
 */
@Getter
@ToString
@AllArgsConstructor
public enum GameVerdict {

    WIN("WON"),
    DRAW("DREW"),
    LOSS("LOST");

    private final String pastTense;
}
