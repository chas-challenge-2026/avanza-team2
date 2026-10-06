package se.comerit.avanza.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface StockPriceHistoryClient {

    record HistoricalPrice(String symbol, LocalDate date, BigDecimal close) {
    }

    /**
     * Fetches historical daily prices for provider-specific market symbols.
     *
     * @param symbols  the symbols expected by the provider
     * @param fromDate the start date for the historical price range
     * @param toDate   the end date for the historical price range
     * @return historical prices, or an empty list when unavailable
     */
    List<HistoricalPrice> fetchHistoricalPrices(List<String> symbols, LocalDate fromDate, LocalDate toDate);
}
