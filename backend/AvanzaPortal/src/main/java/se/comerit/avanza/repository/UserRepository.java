package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.User;

/**
 * UserRepository is a Spring Data JPA repository interface.
 * It is used for accessing User entity from the database.
 * Provides CRUD operations and query methods for User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
