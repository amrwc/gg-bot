package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.GameRequest;
import dev.amrw.ggbot.dto.GameVerdict;
import dev.amrw.ggbot.dto.SlotResult;
import dev.amrw.ggbot.util.EmojiUtil;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/**
 * Slot machine service.
 * <p>
 * Terminology taken from <a href="https://www.onlineunitedstatescasinos.com/online-slots/terms/">here</a>.
 */
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
    private static final Map<String, Double> PAYLINE_MULTIPLIERS = Map.of(
            "🥇🥇", 0.5,
            "💎💎", 2.0,
            "💯💯", 2.0,
            "🥇🥇🥇", 2.5,
            "💎💎💎", 3.0,
            "💵💵", 3.5,
            "💯💯💯", 4.0,
            "💵💵💵", 7.0,
            "💰💰", 7.0,
            "💰💰💰", 15.0
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
        var multiplier = PAYLINE_MULTIPLIERS.get(payline);
        if (null == multiplier) {
            final var firstAndSecondColumn = EmojiUtil.getEmojiSubstring(payline, 0, 2);
            multiplier = PAYLINE_MULTIPLIERS.get(firstAndSecondColumn);
            if (null == multiplier) {
                final var secondAndThirdColumn = EmojiUtil.getEmojiSubstring(payline, 1);
                multiplier = PAYLINE_MULTIPLIERS.get(secondAndThirdColumn);
            }
        }
        return null == multiplier ? 0L : Math.round(bet * multiplier);
    }
}
