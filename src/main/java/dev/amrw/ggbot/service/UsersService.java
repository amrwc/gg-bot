package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.repository.UsersRepository;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.entity.message.MessageAuthor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Service for managing users.
 */
@Log4j2
@Service
public class UsersService {

    private final UsersRepository usersRepository;

    public UsersService(final UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    /**
     * Attempts to find an existing {@link User} with the given Discord user ID, or creates new one if not found.
     * @param messageAuthor author of the message
     * @return existing or new {@link User}
     */
    @Transactional
    public User getOrCreateUser(final MessageAuthor messageAuthor) {
        return usersRepository.findByDiscordUserId(messageAuthor.getIdAsString()).orElseGet(() -> {
            log.debug("Creating new User: discordUserId={}, discordUsername={}",
                    messageAuthor.getIdAsString(), messageAuthor.getName());
            final var newUser = new User();
            newUser.setDiscordUserId(messageAuthor.getIdAsString());
            newUser.setDiscordUsername(messageAuthor.getName());
            return usersRepository.save(newUser);
        });
    }
}
