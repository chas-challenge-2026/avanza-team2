package se.comerit.avanza.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.comerit.avanza.entity.Holdings;

/**
 * HoldingsRepository is a Spring Data JPA repository interface.
 * It is used for accessing Holdings entity from the database.
 * Provides CRUD operations and query methods for Holdings entities.
 */
public interface HoldingsRepository extends JpaRepository<Holdings, Long> {
    // Enforce ownership in the delete itself, not in a separate pre-check.
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Holdings h WHERE h.id = ?1 AND h.account.id IN (SELECT a.id FROM Account a WHERE a.user.id = ?2)")
    int deleteOwnedHolding(Long holdingId, Long userId);
    @Query("""
    SELECT h FROM Holdings h
    JOIN FETCH h.account a
    WHERE a.user.id = :userId
    ORDER BY a.account_type, h.ticker
    """)
    List<Holdings> findHoldingsForUser(@Param("userId") Long userId);

    @Query("SELECT h FROM Holdings h WHERE h.account.user.id = ?1")
    Page<Holdings> findAllByUserId(Long userId, Pageable pageable);

    Page<Holdings> findAllByAccountIdIn(List<Long> accountIds, Pageable pageable);
}
