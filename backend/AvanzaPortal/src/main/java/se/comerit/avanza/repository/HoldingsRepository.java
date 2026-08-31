package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.Holdings;

/**
 * HoldingsRepository is a Spring Data JPA repository interface.
 * It is used for accessing Holdings entity from the database.
 * Provides CRUD operations and query methods for Holdings entities.
 */
public interface HoldingsRepository extends JpaRepository<Holdings, Long> {
}
