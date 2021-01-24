package me.rename.renameme.repository;

import me.rename.renameme.model.WelcomeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Repository for the welcome messages.
 */
@Repository
@Transactional
public interface WelcomeMessageRepository extends JpaRepository<WelcomeMessage, Long> {
}
