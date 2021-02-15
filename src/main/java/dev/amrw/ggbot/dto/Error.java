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

    ALREADY_COLLECTED_DAILY("You have already collected your daily credits today"),
    INSUFFICIENT_CREDITS("You have insufficient credits"),
    NEGATIVE_BET("Bet must be a positive value"),
    UNKNOWN_ERROR("Unknown error");

    private final String message;
}
