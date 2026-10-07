package se.comerit.avanza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.HistoricalStockPrice;

public interface HistoricalRepository extends JpaRepository<HistoricalStockPrice, Long> {
}
