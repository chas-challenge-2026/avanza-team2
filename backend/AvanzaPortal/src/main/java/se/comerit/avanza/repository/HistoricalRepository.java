package se.comerit.avanza.repository;

import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.HistoricalStockPrice;

public interface HistoricalRepository extends JpaRepository<HistoricalStockPrice, Long> {
    /**
     * Finds the most recent historical stock price for the given ticker.
     * 
     * @param ticker the ticker symbol of the stock
     * @return an Optional containing the most recent historical stock price if
     *         available, otherwise an empty Optional
     */
    Optional<HistoricalStockPrice> findFirstByTickerOrderByPriceDateDesc(String ticker);

    /**
     * a range query for historical stock prices.
     *
     * @param tickers   the list of ticker symbols of the stocks
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of historical stock prices matching the criteria
     */
    List<HistoricalStockPrice> findByTickerInAndPriceDateBetween(List<String> tickers, LocalDate startDate,
            LocalDate endDate);
}
