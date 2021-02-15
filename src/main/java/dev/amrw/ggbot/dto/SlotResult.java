package dev.amrw.ggbot.dto;

import lombok.*;

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
    private Error error;

    public Long getNetProfit() {
        return this.creditsWon - this.bet;
    }

    public Boolean hasPlayed() {
        return hasPlayed;
    }
}
