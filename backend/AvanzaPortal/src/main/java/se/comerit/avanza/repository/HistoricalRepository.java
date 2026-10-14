package se.comerit.avanza.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.HistoricalStockPrice;

/**
 * HistoricalRepository is a Spring Data JPA repository interface.
 * It is used for accessing HistoricalStockPrice entity from the database.
 * Provides CRUD operations and query methods for HistoricalStockPrice entities.
 */
public interface HistoricalRepository extends JpaRepository<HistoricalStockPrice, Long> {
    Optional<HistoricalStockPrice> findFirstByTickerOrderByPriceDateDesc(String ticker);
}
