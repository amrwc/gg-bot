package dev.amrw.ggbot.dto;

import lombok.*;

import java.util.Optional;

/**
 * Outcome of a game of slots.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotResult {

    private long bet = 0L;
    /** The game is only played when the user has sufficient credits. */
    @Getter(AccessLevel.NONE)
    private Boolean hasPlayed = false;
    private Long creditsWon = 0L;
    private String payline = "";
    private Long currentBalance = 0L;
    @Getter(AccessLevel.NONE)
    private Error error = null;

    public Long getNetProfit() {
        return this.creditsWon - this.bet;
    }

    public Boolean hasPlayed() {
        return hasPlayed;
    }

    public Optional<Error> getError() {
        return Optional.ofNullable(error);
    }
}
