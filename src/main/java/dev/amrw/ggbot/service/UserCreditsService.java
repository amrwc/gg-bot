package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.repository.UserCreditsRepository;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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
     * @param event {@link MessageCreateEvent}
     * @return existing or new {@link UserCredit}
     */
    public UserCredit getOrCreateUserCredit(final MessageCreateEvent event) {
        final var user = usersService.getOrCreateUser(event);
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
            return userCreditsRepository.save(newUserCredit);
        });
    }

    /**
     * Retrieves the given user their current credits balance.
     * @param event {@link MessageCreateEvent}
     * @return current credits balance
     */
    public Long getCurrentBalance(final MessageCreateEvent event) {
        return getOrCreateUserCredit(event).getCredits();
    }

    /**
     * Adds new credits to the current balance.
     * @param event {@link MessageCreateEvent}
     * @param credits credits to add to the current balance
     * @return new credits balance
     */
    @Transactional
    public Long addCredits(final MessageCreateEvent event, final long credits) {
        final var userCredit = getOrCreateUserCredit(event);
        final var currentCredits = userCredit.getCredits();
        long newCredits;
        try {
            newCredits = Math.addExact(currentCredits, credits);
        } catch (final ArithmeticException exception) {
            log.error("Error adding {} credits to user {}", credits, userCredit.getUser(), exception);
            newCredits = currentCredits;
        }
        userCredit.setCredits(Math.max(newCredits, 0L));
        return userCredit.getCredits();
    }
}
