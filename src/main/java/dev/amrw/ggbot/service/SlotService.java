package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.SlotResult;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
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
    private static final Map<String, Number> PAYLINE_MULTIPLIERS = new LinkedHashMap<>();

    static {
        PAYLINE_MULTIPLIERS.put("🥇🥇🥇", 2.5);
        PAYLINE_MULTIPLIERS.put("🥇🥇", 0.5);
        PAYLINE_MULTIPLIERS.put("💎💎💎", 3);
        PAYLINE_MULTIPLIERS.put("💎💎", 2);
        PAYLINE_MULTIPLIERS.put("💯💯💯", 4);
        PAYLINE_MULTIPLIERS.put("💯💯", 2);
        PAYLINE_MULTIPLIERS.put("💵💵💵", 7);
        PAYLINE_MULTIPLIERS.put("💵💵", 3.5);
        PAYLINE_MULTIPLIERS.put("💰💰💰", 15);
        PAYLINE_MULTIPLIERS.put("💰💰", 7);
    }

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

    protected long calculateWinnings(final long bet, final String payline) {
        for (final Map.Entry<String, Number> multiplier : PAYLINE_MULTIPLIERS.entrySet()) {
            if (payline.contains(multiplier.getKey())) {
                return Math.round(bet * multiplier.getValue().doubleValue());
            }
        }
        return 0L;
    }

    private String spin() {
        final var rollBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            final var index = random.nextInt(SYMBOLS.size());
            rollBuilder.append(SYMBOLS.get(index));
        }
        return rollBuilder.toString();
    }
}
