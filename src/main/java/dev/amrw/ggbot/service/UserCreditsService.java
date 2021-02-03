package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.repository.UserCreditsRepository;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.entity.message.MessageAuthor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * Service for managing user credits.
 */
@Log4j2
@Service
public class UserCreditsService {

    private final UsersService usersService;
    private final UserCreditsRepository userCreditsRepository;

    public UserCreditsService(final UsersService usersService, final UserCreditsRepository userCreditsRepository) {
        this.usersService = usersService;
        this.userCreditsRepository = userCreditsRepository;
    }

    /**
     * Attempts to find an existing {@link UserCredit} of the given {@link User}, or creates new one if not found.
     * @param messageAuthor author of the message
     * @return existing or new {@link UserCredit}
     */
    public UserCredit getOrCreateUserCredit(final MessageAuthor messageAuthor) {
        final var user = usersService.getOrCreateUser(messageAuthor);
        return getOrCreateUserCredit(user);
    }

    /**
     * Attempts to find an existing {@link UserCredit} of the given {@link User}, or creates new one if not found.
     * @param user user
     * @return existing or new {@link UserCredit}
     */
    @Transactional
    public UserCredit getOrCreateUserCredit(final User user) {
        return userCreditsRepository.findUserCreditByUser(user).orElseGet(() -> {
            log.debug("Creating new UserCredit: user={}", user.toString());
            final var newUserCredit = new UserCredit();
            newUserCredit.setUser(user);
            newUserCredit.setCredits(0L);
            return userCreditsRepository.save(newUserCredit);
        });
    }

    /**
     * Retrieves the given user their current credits balance.
     * @param messageAuthor author of the message
     * @return current credits balance
     */
    public Long getCurrentBalance(final MessageAuthor messageAuthor) {
        return getOrCreateUserCredit(messageAuthor).getCredits();
    }

    /**
     * Adds credit to the user's account if they claim it after the configured time has elapsed.
     * @param messageAuthor Discord-native user ID
     * @param newCredits amount of credits to claim
     * @return whether the credits have been applied
     */
    @Transactional
    public boolean claimDailyCredits(final MessageAuthor messageAuthor, final Long newCredits) {
        final var user = usersService.getOrCreateUser(messageAuthor);
        return Optional.ofNullable(user.getUserCredit()).map(userCredit -> {
            final var cooldownStart = Date.from(Instant.now().minus(30L, ChronoUnit.SECONDS));
            if (userCredit.getLastDaily().after(cooldownStart)) {
                return false;
            }
            userCredit.setCredits(newCredits + userCredit.getCredits());
            userCredit.setLastDaily(new Date());
            return true;
        }).orElseGet(() -> {
            final var userCredit = getOrCreateUserCredit(user);
            userCredit.setCredits(newCredits);
            userCredit.setLastDaily(new Date());
            user.setUserCredit(userCredit);
            return true;
        });
    }
}
