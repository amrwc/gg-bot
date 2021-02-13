package dev.amrw.ggbot.repository;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for <code>USER_CREDITS</code> table.
 */
@Repository
@Transactional
public interface UserCreditsRepository extends JpaRepository<UserCredit, UUID> {

    Optional<UserCredit> findUserCreditByUser(User user);
}
