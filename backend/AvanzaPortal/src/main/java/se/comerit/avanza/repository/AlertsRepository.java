package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.Alerts;
import java.util.List;

/**
 * AlertsRepository is a Spring Data JPA repository interface.
 * It is used for accessing Alerts entity from the database.
 * Provides CRUD operations and query methods for Alerts entities.
 */
public interface AlertsRepository extends JpaRepository<Alerts, Long> {

    List<Alerts> findByUserIdOrderByCreated_atDesc(Long userId);

    List<Alerts> findByUser_IdAndDismissedFalseOrderByCreatedAtDesc(Long userId);
}
