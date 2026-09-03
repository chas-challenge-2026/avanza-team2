package se.comerit.avanza.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.Alerts;
import java.util.List;

/**
 * AlertsRepository is a Spring Data JPA repository interface.
 * It is used for accessing Alerts entity from the database.
 * Provides CRUD operations and query methods for Alerts entities, including
 * support for pagination.
 */
public interface AlertsRepository extends JpaRepository<Alerts, Long> {
    /**
     * Finds alerts for a specific user, ordered by creation date in descending
     * order, with pagination support via Spring Data JPA.
     */
    Page<Alerts> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Finds alerts for a specific user that have not been dismissed, ordered by
     * creation date in descending order, with pagination support via Spring Data
     * JPA.
     */
    List<Alerts> findByUser_IdAndDismissedFalseOrderByCreatedAtDesc(Long userId);
}
