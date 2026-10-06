package se.comerit.avanza.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface StockPriceHistoryClient {

    record HistoricalPrice(String symbol, LocalDate date, BigDecimal close) {
    }

    /**
     * Fetches historical daily prices for a provider-specific market symbol.
     *
     * @param symbol   the symbol expected by the provider
     * @param exchange the exchange where the instrument is listed
     * @param fromDate the start date for the historical price range
     * @param toDate   the end date for the historical price range
     * @return historical prices, or an empty list when unavailable
     */
    List<HistoricalPrice> fetchHistoricalPrices(String symbol, String exchange, LocalDate fromDate, LocalDate toDate);
}
