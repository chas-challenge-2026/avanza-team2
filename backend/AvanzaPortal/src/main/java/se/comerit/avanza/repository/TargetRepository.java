package se.comerit.avanza.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.TargetAllocations;

/**
 * TargetRepository is a Spring Data JPA repository interface.
 * It is used for accessing TargetAllocations entity from the database.
 * Provides CRUD operations and query methods for TargetAllocations entities.
 */
public interface TargetRepository extends JpaRepository<TargetAllocations, Long> {

    List<TargetAllocations> findByUserId(Long userId);
}