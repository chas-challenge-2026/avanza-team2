package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import se.comerit.avanza.client.StockPriceClient;
import se.comerit.avanza.client.StockPriceHistoryClient;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.nativebridge.FxLibrary;
import se.comerit.avanza.repository.HistoricalRepository;

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

    private static final List<String> HISTORICAL_TICKERS = List.of(
            "ERIC-B", "VOLV-B", "AAPL", "SWED-A", "SAND");

    // The FX library used to fetch foreign exchange rates.
    private final FxLibrary fxLibrary;
    private final StockPriceClient stockPriceClient;
    private final Clock clock;
    private final int historicalLookbackDays;
    private final HistoricalRepository historicalRepository;
    private final StockPriceHistoryClient historicalDataClient;
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();

    @Autowired
    public MarketService(
            FxLibrary fxLibrary,
            StockPriceClient stockPriceClient,
            HistoricalRepository historicalRepository,
            StockPriceHistoryClient historicalDataClient,
            @Value("${marketstack.history.lookback-days:360}") int lookbackDays) {
        this(fxLibrary, stockPriceClient, historicalRepository, historicalDataClient,
                Clock.systemUTC(), lookbackDays);
    }

    MarketService(
            FxLibrary fxLibrary,
            StockPriceClient stockPriceClient,
            HistoricalRepository historicalRepository,
            StockPriceHistoryClient historicalDataClient,
            Clock clock,
            int lookbackDays) {
        this.fxLibrary = fxLibrary;
        this.stockPriceClient = stockPriceClient;
        this.clock = clock;
        this.historicalLookbackDays = lookbackDays;
        this.historicalRepository = historicalRepository;
        this.historicalDataClient = historicalDataClient;
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

    // Hardcoded prices was in v1 and now servs as fallback
    public Map<String, Double> getPriceFallback() {
        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("ERIC-B", 74.20);
        currentPrices.put("VOLV-B", 268.50);
        currentPrices.put("AAPL", 187.32);
        currentPrices.put("SWED-A", 193.10);
        currentPrices.put("SAND", 212.80);
        currentPrices.put("DEFAULT", 100.0);
        return currentPrices;
    }

    /**
     * Imports recent history for the supported tickers, continuing from each
     * ticker's latest stored price when available.
     *
     * @return the number of historical stock prices imported
     */
    @Transactional
    public int importHistoricalData() {
        if (historicalLookbackDays <= 0) {
            return 0;
        }

        LocalDate toDate = LocalDate.now(clock).minusDays(1);
        LocalDate initialFromDate = toDate.minusDays(historicalLookbackDays - 1L);
        Map<String, LocalDate> nextDateByTicker = new HashMap<>();

        for (String ticker : HISTORICAL_TICKERS) {
            LocalDate nextDate = historicalRepository
                    .findFirstByTickerOrderByPriceDateDesc(ticker)
                    .map(price -> price.getPriceDate().plusDays(1))
                    .orElse(initialFromDate);

            nextDateByTicker.put(ticker, nextDate);
        }

        LocalDate fromDate = nextDateByTicker.values().stream()
                .min(LocalDate::compareTo)
                .orElseThrow();

        if (fromDate.isAfter(toDate)) {
            return 0;
        }

        List<HistoricalStockPrice> pricesToSave = historicalDataClient
                .fetchHistoricalPrices(HISTORICAL_TICKERS, fromDate, toDate)
                .stream()
                .filter(price -> {
                    LocalDate nextDate = nextDateByTicker.get(price.symbol());
                    return nextDate != null
                            && !price.date().isBefore(nextDate)
                            && !price.date().isAfter(toDate);
                })
                .map(price -> new HistoricalStockPrice(
                        price.symbol(), price.date(), price.close()))
                .toList();

        historicalRepository.saveAll(pricesToSave);
        return pricesToSave.size();
    }
}
