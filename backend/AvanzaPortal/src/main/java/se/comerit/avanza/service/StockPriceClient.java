package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Defines how stock prices are retrieved from an external market-data provider.
 */
public interface StockPriceClient {

    /**
     * Fetches the latest available price for a provider-specific market symbol.
     *
     * @param symbol   the symbol expected by the provider
     * @param exchange the exchange where the instrument is listed
     * @return the price, or empty when it cannot be retrieved
     */
    Optional<BigDecimal> fetchPrice(String symbol, String exchange);
}
