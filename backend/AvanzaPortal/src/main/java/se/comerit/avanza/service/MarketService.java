package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import se.comerit.avanza.client.StockPriceClient;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.nativebridge.FxLibrary;

/**
 * Provides market data such as foreign exchange rates and stock prices.
 */
@Service
public class MarketService {

    static final long PRICE_CACHE_TTL_MILLIS = 5 * 60 * 1000L;

    private static final Map<String, MarketSymbol> MARKET_SYMBOLS = Map.of(
            "ERIC-B", new MarketSymbol("ERIC-B.ST", "XSTO"),
            "VOLV-B", new MarketSymbol("VOLV-B.ST", "XSTO"),
            "AAPL", new MarketSymbol("AAPL", "XNAS"),
            "SWED-A", new MarketSymbol("SWED-A.ST", "XSTO"),
            "SAND", new MarketSymbol("SAND.ST", "XSTO"));

    // The FX library used to fetch foreign exchange rates.
    private final FxLibrary fxLibrary;
    private final StockPriceClient stockPriceClient;
    private final Clock clock;
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();

    public MarketService(FxLibrary fxLibrary, StockPriceClient stockPriceClient) {
        this(fxLibrary, stockPriceClient, Clock.systemUTC());
    }

    MarketService(FxLibrary fxLibrary, StockPriceClient stockPriceClient, Clock clock) {
        this.fxLibrary = fxLibrary;
        this.stockPriceClient = stockPriceClient;
        this.clock = clock;
    }

    /**
     * Retrieves the foreign exchange rate for the given currency pair.
     *
     * @param from the base currency
     * @param to   the quote currency
     * @return the FX rate response DTO
     * @throws IllegalArgumentException if the currency pair is unknown
     */
    public FxRateResponseDTO getFx(String from, String to) {
        // Get the current date in epoch seconds at the start of the day (UTC)
        long today = LocalDate.now()
                .atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond();

        // Fetch the FX rate for the given currency pair and date.
        double rate = fxLibrary.fx_rate(from.toUpperCase(), to.toUpperCase(), today);

        // If the rate is negative, it indicates an unknown currency pair.
        if (rate < 0) {
            throw new IllegalArgumentException("Unknown currency pair: " + from + "/" + to);
        }

        // Return the FX rate response DTO with the current date, base currency, quote
        // currency, and rate.
        return new FxRateResponseDTO(
                LocalDate.now().toString(),
                from.toUpperCase(),
                to.toUpperCase(),
                rate);
    }

    /**
     * Returns a cached or externally fetched price for a supported ticker.
     *
     * @param ticker the application's ticker symbol
     * @return the latest available price, or empty when unavailable
     */
    public Optional<BigDecimal> getPrice(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            return Optional.empty();
        }

        String normalizedTicker = ticker.trim().toUpperCase();
        MarketSymbol marketSymbol = MARKET_SYMBOLS.get(normalizedTicker);

        if (marketSymbol == null) {
            return Optional.empty();
        }

        CachedPrice cachedPrice = priceCache.get(normalizedTicker);

        if (cachedPrice != null && clock.millis() < cachedPrice.expiresAtMillis()) {
            return Optional.of(cachedPrice.price());
        }

        if (cachedPrice != null) {
            priceCache.remove(normalizedTicker, cachedPrice);
        }

        Optional<BigDecimal> fetchedPrice = stockPriceClient.fetchPrice(
                marketSymbol.symbol(),
                marketSymbol.exchange());

        fetchedPrice.ifPresent(price -> priceCache.put(
                normalizedTicker,
                new CachedPrice(price, clock.millis() + PRICE_CACHE_TTL_MILLIS)));
        return fetchedPrice;
    }

    private record CachedPrice(BigDecimal price, long expiresAtMillis) {
    }

    private record MarketSymbol(String symbol, String exchange) {
    }
}
