package dev.amrw.ggbot.service;

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

    private static final List<String> SYMBOLS = List.of("🥇", "💎", "💯", "💵", "💰");
    private static final Map<String, Double> PAYLINE_MULTIPLIERS = Map.of(
            "🥇🥇🥇", 2.5,
            "🥇🥇", 0.5,
            "💎💎💎", 3.0,
            "💎💎", 2.0,
            "💯💯💯", 4.0,
            "💯💯", 2.0,
            "💵💵💵", 7.0,
            "💵💵", 3.5,
            "💰💰💰", 15.0,
            "💰💰", 7.0
    );

    private final SecureRandom random;

    public SlotService() {
        this.random = new SecureRandom();
    }

    /**
     * Play a game of slots with the given bet.
     * @param bet number of credits on the line
     * @return result of the game
     */
    public SlotResult play(final long bet) {
        final var payline = spin();
        return new SlotResult(bet, calculateWinnings(bet, payline), payline);
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
