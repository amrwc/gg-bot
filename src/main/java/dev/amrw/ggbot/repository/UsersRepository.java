package dev.amrw.ggbot.repository;

import dev.amrw.ggbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for <code>USERS</code> table.
 */
@Repository
@Transactional
public interface UsersRepository extends JpaRepository<User, UUID> {

    Optional<User> findByDiscordUserId(String discordUserId);
}
