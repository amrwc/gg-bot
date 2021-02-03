package dev.amrw.ggbot.repository;

import dev.amrw.ggbot.model.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

/**
 * Repository for `USER_CREDITS` table.
 */
@Repository
@Transactional
public interface UserCreditsRepository extends JpaRepository<UserCredit, UUID> {
}
