package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import se.comerit.avanza.entity.Holdings;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * HoldingsRepository is a Spring Data JPA repository interface.
 * It is used for accessing Holdings entity from the database.
 * Provides CRUD operations and query methods for Holdings entities.
 */
public interface HoldingsRepository extends JpaRepository<Holdings, Long> {
    @Query("SELECT h FROM Holdings h WHERE h.account.user.id = ?1")
    List<Holdings> findAllByUserId(Long userId, Pageable pageable);

    List<Holdings> findAllByAccountIdIn(List<Long> accountIds, Pageable pageable);
}
