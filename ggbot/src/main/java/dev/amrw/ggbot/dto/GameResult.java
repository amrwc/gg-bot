package dev.amrw.ggbot.dto;

import lombok.*;

import java.util.Optional;

/**
 * Result of an arbitrary game.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResult {

    private Long bet = 0L;
    /** The game is only played when the user has sufficient credits. */
    @Getter(AccessLevel.NONE)
    private Boolean hasPlayed = false;
    private GameVerdict verdict = GameVerdict.DRAW;
    private Long creditsWon = 0L;
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
