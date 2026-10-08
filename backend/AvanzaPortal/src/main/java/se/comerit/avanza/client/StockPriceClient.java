package se.comerit.avanza.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

    /**
     * Fetches the latest prices for multiple provider-specific symbols in one
     * request.
     *
     * @param symbols the symbols expected by the provider
     * @return available prices keyed by provider-specific symbol
     */
    Map<String, BigDecimal> fetchPrices(List<String> symbols);
}
