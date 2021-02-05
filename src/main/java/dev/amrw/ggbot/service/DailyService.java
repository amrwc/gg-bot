package dev.amrw.ggbot.service;

import org.javacord.api.entity.message.MessageAuthor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/**
 * Service for managing daily operations.
 */
@Service
public class DailyService {

    public static final Duration DEFAULT_COOLDOWN = Duration.ofDays(1L);
    static final long DEFAULT_DAILY_CREDITS = 2_500L;

    private final UsersService usersService;
    private final UserCreditsService userCreditsService;

    public DailyService(final UsersService usersService, final UserCreditsService userCreditsService) {
        this.usersService = usersService;
        this.userCreditsService = userCreditsService;
    }

    /**
     * Adds credit to the user's account if they claim it after the configured time has elapsed.
     * @param messageAuthor author of the Discord message
     * @return number of credits that have been added; <code>0L</code> if the user cannot yet claim the daily credits
     */
    @Transactional
    public long claimDailyCredits(final MessageAuthor messageAuthor) {
        final var user = usersService.getOrCreateUser(messageAuthor);
        return Optional.ofNullable(user.getUserCredit()).map(userCredit -> {
            if (!userCredit.canClaimDailyCredits()) {
                return 0L;
            }
            userCredit.setCredits(DEFAULT_DAILY_CREDITS + userCredit.getCredits());
            userCredit.setLastDaily(new Date());
            return DEFAULT_DAILY_CREDITS;
        }).orElseGet(() -> {
            final var userCredit = userCreditsService.getOrCreateUserCredit(user);
            userCredit.setCredits(DEFAULT_DAILY_CREDITS);
            userCredit.setLastDaily(new Date());
            user.setUserCredit(userCredit);
            return DEFAULT_DAILY_CREDITS;
        });
    }
}
