package se.comerit.avanza.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface StockPriceHistoryClient {
    /**
     * Fetches the historical price for a provider-specific market symbol.
     *
     * @param symbol   the symbol expected by the provider
     * @param exchange the exchange where the instrument is listed
     * @param date     the date for which the historical price is requested
     * @return the price, or empty when it cannot be retrieved
     */
    Optional<BigDecimal> fetchHistoricalPrice(String symbol, String exchange, LocalDate date);
}
