package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.repository.UserCreditsRepository;
import dev.amrw.ggbot.repository.UserRepository;
import org.javacord.api.entity.message.MessageAuthor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service for managing user credits.
 */
@Service
public class UserCreditsService {

    private final UserRepository userRepository;
    private final UserCreditsRepository userCreditsRepository;

    public UserCreditsService(final UserRepository userRepository, final UserCreditsRepository userCreditsRepository) {
        this.userRepository = userRepository;
        this.userCreditsRepository = userCreditsRepository;
    }

    /**
     * Adds credit to the user's account if they claim it after the configured time has elapsed.
     * @param messageAuthor Discord-native user ID
     * @param credits amount of credits to claim
     */
    @Transactional
    public boolean claimDailyCredits(final MessageAuthor messageAuthor, final Long credits) {
        final var user = userRepository.findByDiscordUserId(messageAuthor.getIdAsString()).orElseGet(() -> {
            final var newUser = new User();
            newUser.setDiscordUserId(messageAuthor.getIdAsString());
            newUser.setDiscordUsername(messageAuthor.getName());
            return userRepository.save(newUser);
        });
        final var credit = user.getUserCredit();
        if (null == credit) {
            var newCredit = new UserCredit();
            newCredit.setUser(user);
            newCredit.setCredits(credits);
            newCredit.setLastDaily(Date.from(Instant.now()));
            newCredit = userCreditsRepository.save(newCredit);
            user.setUserCredit(newCredit);
        } else {
            final var cooldownStart = Date.from(Instant.now().minus(30L, ChronoUnit.SECONDS));
            if (credit.getLastDaily().after(cooldownStart)) {
                return false;
            }
            credit.setCredits(credits + credit.getCredits());
        }
        return true;
    }
}
