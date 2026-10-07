package se.comerit.avanza.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.comerit.avanza.entity.Account;


/**
 * AccountRepository is a Spring Data JPA repository interface.
 * It is used for accessing Account entity from the database.
 * Provides CRUD operations and query methods for Account entities.
 */

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);
    java.util.List<Account> findAllByUser_Id(Long userId);
    boolean existsByIdAndUser_Id(Long accountId, Long UserId);

    @Query("""
            SELECT DISTINCT account
            FROM Account account
            LEFT JOIN FETCH account.holdings
            WHERE account.user.id = :userId
            """)
    List<Account> findAllWithHoldingsByUserId(@Param("userId") Long userId);
}
