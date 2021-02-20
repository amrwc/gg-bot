package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.*;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;

/**
 * Roshambo (Rock, Paper, Scissors) service.
 */
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
        if (GameVerdict.WIN.equals(verdict)) {
            return bet * 2;
        } else if (GameVerdict.DRAW.equals(verdict)) {
            return bet;
        } else {
            return 0L;
        }
    }
}