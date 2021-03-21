package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;

/**
 * Roshambo (Rock, Paper, Scissors) service.
 */
@Log4j2
@Service
public class RoshamboService {

    static final Map<RoshamboShape, Map<RoshamboShape, GameVerdict>> RULES = Map.of(
            RoshamboShape.ROCK, Map.of(
                    RoshamboShape.ROCK, GameVerdict.DRAW,
                    RoshamboShape.PAPER, GameVerdict.LOSS,
                    RoshamboShape.SCISSORS, GameVerdict.WIN
            ),
            RoshamboShape.PAPER, Map.of(
                    RoshamboShape.ROCK, GameVerdict.WIN,
                    RoshamboShape.PAPER, GameVerdict.DRAW,
                    RoshamboShape.SCISSORS, GameVerdict.LOSS
            ),
            RoshamboShape.SCISSORS, Map.of(
                    RoshamboShape.ROCK, GameVerdict.LOSS,
                    RoshamboShape.PAPER, GameVerdict.WIN,
                    RoshamboShape.SCISSORS, GameVerdict.DRAW
            )
    );
    private static final Map<GameVerdict, Long> MULTIPLIERS = Map.of(
            GameVerdict.WIN, 2L,
            GameVerdict.DRAW, 1L,
            GameVerdict.LOSS, 0L
    );
    private static final RoshamboShape[] SHAPES = RoshamboShape.values();

    private final UserCreditsService userCreditsService;
    private final SecureRandom random;

    public RoshamboService(final UserCreditsService userCreditsService) {
        this.userCreditsService = userCreditsService;
        this.random = new SecureRandom();
    }

    /**
     * Plays a game of Roshambo with the given bet and shape.
     * @param request {@link RoshamboRequest}
     * @return result of the game
     */
    public RoshamboResult play(final RoshamboRequest request) {
        final var currentBalance = userCreditsService.getCurrentBalance(request.getEvent());
        if (request.getBet() > currentBalance) {
            log.debug("Didn't play because of insufficient credits {}", request.getEvent().getMessageAuthor());
            final var result = new RoshamboResult();
            result.setBet(request.getBet());
            result.setHasPlayed(false);
            result.setCurrentBalance(currentBalance);
            result.setError(Error.INSUFFICIENT_CREDITS);
            return result;
        }

        final var randomisedShape = randomiseShape();
        final var verdict = RULES.get(request.getShape()).get(randomisedShape);
        final var winnings = calculateWinnings(request.getBet(), verdict);

        final var result = new RoshamboResult();
        result.setBet(request.getBet());
        result.setHasPlayed(true);
        result.setVerdict(verdict);
        result.setCreditsWon(winnings);
        result.setCurrentBalance(userCreditsService.addCredits(request.getEvent(), result.getNetProfit()));
        result.setShape(randomisedShape);
        return result;
    }

    protected RoshamboShape randomiseShape() {
        final var index = random.nextInt(SHAPES.length);
        return SHAPES[index];
    }

    protected long calculateWinnings(final Long bet, final GameVerdict verdict) {
        final var multiplier = MULTIPLIERS.get(verdict);
        if (Objects.equals(0L, multiplier)) {
            return 0L;
        } else if (Objects.equals(1L, multiplier)) {
            return bet;
        }

        try {
            return Math.multiplyExact(bet, multiplier);
        } catch (final ArithmeticException exception) {
            final var longOverflow = "long overflow".equals(exception.getMessage());
            log.error("Error calculating winnings (bet={} * multiplier={}). Defaulting to {}", bet, multiplier,
                    longOverflow ? "Long.MAX_VALUE" : "bet value", exception);
            return longOverflow ? Long.MAX_VALUE : bet;
        }
    }
}
