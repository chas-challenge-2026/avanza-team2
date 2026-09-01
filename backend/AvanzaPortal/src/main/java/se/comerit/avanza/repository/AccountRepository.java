package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.Account;

/**
 * AccountRepository is a Spring Data JPA repository interface.
 * It is used for accessing Account entity from the database.
 * Provides CRUD operations and query methods for Account entities.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    java.util.List<Account> findAllByUser_Id(Long userId);
}
