package se.comerit.avanza.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.HistoricalStockPrice;

public interface HistoricalRepository extends JpaRepository<HistoricalStockPrice, Long> {
    Optional<HistoricalStockPrice> findFirstByTickerOrderByPriceDateDesc(String ticker);
}
