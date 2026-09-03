package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.TargetAllocations;
import java.util.List;

/**
 * TargetRepository is a Spring Data JPA repository interface.
 * It is used for accessing TargetAllocations entity from the database.
 * Provides CRUD operations and query methods for TargetAllocations entities.
 */
public interface TargetRepository extends JpaRepository<TargetAllocations, Long> {
    List<TargetAllocations> findByUser_Id(Long userId);
}
