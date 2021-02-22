package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.GameRequest;
import dev.amrw.ggbot.dto.GameVerdict;
import dev.amrw.ggbot.dto.SlotResult;
import dev.amrw.ggbot.util.EmojiUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Slot machine service.
 * <p>
 * Terminology taken from <a href="https://www.onlineunitedstatescasinos.com/online-slots/terms/">here</a>.
 */
@Log4j2
@Service
public class SlotService {

    /** Bag of symbols that will be selected randomly. The more of them there is, the more common they are. */
    static final List<String> SYMBOLS = List.of(
            "🥇", "🥇", "🥇", "🥇", "🥇", "🥇", "🥇", "🥇", "🥇",
            "💎", "💎", "💎", "💎", "💎", "💎",
            "💯", "💯", "💯", "💯",
            "💵", "💵",
            "💰"
    );
    private static final Map<String, BigDecimal> PAYLINE_MULTIPLIERS = Map.of(
            "🥇🥇", BigDecimal.valueOf(0.5),
            "💎💎", BigDecimal.valueOf(2.0),
            "💯💯", BigDecimal.valueOf(2.0),
            "🥇🥇🥇", BigDecimal.valueOf(2.5),
            "💎💎💎", BigDecimal.valueOf(3.0),
            "💵💵", BigDecimal.valueOf(3.5),
            "💯💯💯", BigDecimal.valueOf(4.0),
            "💵💵💵", BigDecimal.valueOf(7.0),
            "💰💰", BigDecimal.valueOf(7.0),
            "💰💰💰", BigDecimal.valueOf(15.0)
    );

    private final UserCreditsService userCreditsService;
    private final SecureRandom random;

    public SlotService(final UserCreditsService userCreditsService) {
        this.userCreditsService = userCreditsService;
        this.random = new SecureRandom();
    }

    /**
     * Play a game of slots with the given bet.
     * @param request slot game request
     * @return result of the game
     */
    public SlotResult play(final GameRequest request) {
        final var currentBalance = userCreditsService.getCurrentBalance(request.getEvent());
        if (request.getBet() > currentBalance) {
            final var result = new SlotResult();
            result.setBet(request.getBet());
            result.setHasPlayed(false);
            result.setCurrentBalance(currentBalance);
            result.setError(Error.INSUFFICIENT_CREDITS);
            return result;
        }

        final var payline = spin();
        final var winnings = calculateWinnings(request.getBet(), payline);
        final var newBalance = userCreditsService.addCredits(request.getEvent(), winnings - request.getBet());

        final var result = new SlotResult();
        result.setBet(request.getBet());
        result.setHasPlayed(true);
        result.setVerdict(winnings > 0L ? GameVerdict.WIN : GameVerdict.LOSS);
        result.setCreditsWon(winnings);
        result.setCurrentBalance(newBalance);
        result.setPayline(payline);
        return result;
    }

    protected String spin() {
        final var rollBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            final var index = random.nextInt(SYMBOLS.size());
            rollBuilder.append(SYMBOLS.get(index));
        }
        return rollBuilder.toString();
    }

    protected long calculateWinnings(final long bet, final String payline) {
        final var multiplier = getMultiplier(payline);
        if (multiplier.isEmpty()) {
            return 0L;
        }

        // Since `Math.multiplyExact()` has no signature allowing for floating point numbers, the calculations are done
        // using BigDecimals
        try {
            return BigDecimal.valueOf(bet).multiply(multiplier.get())
                    .setScale(0, RoundingMode.HALF_UP) // Drop all decimal points, round half-up
                    .longValueExact();
        } catch (final ArithmeticException exception) {
            log.error("Error calculating winnings: {} * {}", bet, multiplier, exception);
            return Long.MAX_VALUE;
        }
    }

    private Optional<BigDecimal> getMultiplier(final String payline) {
        final var potentialCombinations = new String[] {
                payline, // All three columns
                EmojiUtil.getEmojiSubstring(payline, 0, 2), // First and second column
                EmojiUtil.getEmojiSubstring(payline, 1) // Second and third column
        };
        for (final var combination : potentialCombinations) {
            if (PAYLINE_MULTIPLIERS.containsKey(combination)) {
                return Optional.of(PAYLINE_MULTIPLIERS.get(combination));
            }
        }
        return Optional.empty();
    }
}
