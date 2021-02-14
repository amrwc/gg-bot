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
    private boolean hasPlayed = false;
    private long creditsWon = 0L;
    private String payline = "";
    private long currentBalance = 0L;
    private Error error;

    public long getNetProfit() {
        return this.creditsWon - this.bet;
    }

    public boolean hasPlayed() {
        return hasPlayed;
    }
}
