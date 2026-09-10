package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.User;

import java.util.Optional;

/**
 * Repository for accessing and managing User entities in the database.
 *
 * Spring Data JPA provides the CRUD operations automatically.
 * Custom query methods can be defined based on the entity field names.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
}