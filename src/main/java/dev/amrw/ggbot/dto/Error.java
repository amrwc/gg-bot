package dev.amrw.ggbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Enum representing errors.
 */
@Getter
@ToString
@AllArgsConstructor
public enum Error {

    INSUFFICIENT_CREDITS("You have insufficient credits"),
    NEGATIVE_BET("Bet must be a positive value"),
    UNKNOWN_ERROR("Unknown error");

    private final String message;
}
