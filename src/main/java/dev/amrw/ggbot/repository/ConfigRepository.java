package dev.amrw.ggbot.repository;

import dev.amrw.ggbot.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

/**
 * Repository for <code>CONFIG</code> table.
 */
@Repository
@Transactional
public interface ConfigRepository extends JpaRepository<Config, UUID> {

    @Query("SELECT c.value FROM Config c WHERE c.name = ?1")
    Boolean findBoolean(String name);

    @Query("SELECT c.value FROM Config c WHERE c.name = ?1")
    Double findDouble(String name);

    @Query("SELECT c.value FROM Config c WHERE c.name = ?1")
    Long findLong(String name);

    @Query("SELECT c.value FROM Config c WHERE c.name = ?1")
    String findString(String name);
}
