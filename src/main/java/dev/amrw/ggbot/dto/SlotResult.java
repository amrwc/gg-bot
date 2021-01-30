package dev.amrw.ggbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outcome of a game of slots.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotResult {

    private long bet;
    private long creditsWon;
    private String payline;

    public long getNetProfit() {
        return this.creditsWon - this.bet;
    }
}
