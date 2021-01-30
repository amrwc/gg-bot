package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.SlotResult;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;

/**
 * Slot machine service.
 */
@Service
public class SlotService {

    private static final Map<Integer, String> CAROUSEL = Map.of(
            1, "💎",
            2, "💯",
            3, "🥇",
            4, "💵",
            5, "💰"
    );

    private final SecureRandom random;

    public SlotService() {
        this.random = new SecureRandom();
    }

    public SlotResult play(final long bet) {
        final var rollBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            rollBuilder.append(CAROUSEL.get(random.nextInt(CAROUSEL.size()) + 1));
        }
        final var roll = rollBuilder.toString();
        return new SlotResult(bet, calculateWinnings(bet, roll), roll);
    }

    protected long calculateWinnings(final long bet, final String roll) {
        switch (roll) {
            case "🥇🥇🥇":
                return Math.round(2.5 * bet);
            case "💎💎💎":
                return 3L * bet;
            case "💯💯💯":
                return 4L * bet;
            case "💵💵💵":
                return 7L * bet;
            case "💰💰💰":
                return 15L * bet;
        }

        if (roll.startsWith("🥇🥇") || roll.endsWith("🥇🥇")) {
            return Math.round(0.5 * bet);
        } else if (roll.startsWith("💎💎") || roll.endsWith("💎💎")
                || roll.startsWith("💯💯") || roll.endsWith("💯💯")) {
            return 2L * bet;
        } else if (roll.startsWith("💵💵") || roll.endsWith("💵💵")) {
            return Math.round(3.5 * bet);
        }

        return 0;
    }
}
