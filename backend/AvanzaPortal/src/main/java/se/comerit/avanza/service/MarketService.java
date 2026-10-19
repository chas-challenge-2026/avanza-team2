package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import se.comerit.avanza.client.StockPriceClient;
import se.comerit.avanza.client.HistoricalPriceClient;
import se.comerit.avanza.dto.market.BacktestRequestDTO;
import se.comerit.avanza.dto.market.BacktestResponseDTO;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.nativebridge.BacktestInputBuilder;
import se.comerit.avanza.nativebridge.BacktestLibrary;
import se.comerit.avanza.nativebridge.BacktestResultStructure;
import se.comerit.avanza.nativebridge.FxLibrary;
import se.comerit.avanza.repository.HistoricalRepository;
import com.sun.jna.Pointer;

/**
 * Provides market data such as foreign exchange rates and stock prices.
 */
@Service
public class MarketService {

    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);

    static final long PRICE_CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L;

    private static final Map<String, MarketSymbol> MARKET_SYMBOLS = Map.of(
            "ERIC-B", new MarketSymbol("ERIC-B.ST", "XSTO"),
            "VOLV-B", new MarketSymbol("VOLV-B.ST", "XSTO"),
            "AAPL", new MarketSymbol("AAPL", "XNAS"),
            "SWED-A", new MarketSymbol("SWED-A.ST", "XSTO"),
            "SAND", new MarketSymbol("SAND.ST", "XSTO"));

    private static final List<String> HISTORICAL_TICKERS = List.of(
            "ERIC-B", "VOLV-B", "AAPL", "SWED-A", "SAND");
    private static final Set<String> BACKTEST_STRATEGIES = Set.of("BUY_HOLD", "REBALANCE_MONTHLY");

    // The FX library used to fetch foreign exchange rates.
    private final FxLibrary fxLibrary;
    private final StockPriceClient stockPriceClient;
    private final Clock clock;
    private final int historicalLookbackDays;
    private final HistoricalRepository historicalRepository;
    private final HistoricalPriceClient historicalDataClient;
    private final BacktestLibrary backtestLibrary;
    private final BacktestInputBuilder backtestInputBuilder;
    private volatile Map<String, BigDecimal> priceCache = Map.of();
    private boolean priceRefreshAttempted;
    private long nextPriceRefreshAtMillis;

    @Autowired
    public MarketService(
            FxLibrary fxLibrary,
            StockPriceClient stockPriceClient,
            HistoricalRepository historicalRepository,
            HistoricalPriceClient historicalDataClient,
            BacktestLibrary backtestLibrary,
            @Value("${marketstack.history.lookback-days:360}") int lookbackDays) {
        this(fxLibrary, stockPriceClient, historicalRepository, historicalDataClient, backtestLibrary,
                Clock.systemUTC(), lookbackDays);
    }

    MarketService(
            FxLibrary fxLibrary,
            StockPriceClient stockPriceClient,
            HistoricalRepository historicalRepository,
            HistoricalPriceClient historicalDataClient,
            BacktestLibrary backtestLibrary,
            Clock clock,
            int lookbackDays) {
        this.fxLibrary = fxLibrary;
        this.stockPriceClient = stockPriceClient;
        this.clock = clock;
        this.historicalLookbackDays = lookbackDays;
        this.historicalRepository = historicalRepository;
        this.historicalDataClient = historicalDataClient;
        this.backtestLibrary = backtestLibrary;
        this.backtestInputBuilder = new BacktestInputBuilder(historicalRepository);
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

        refreshPricesIfNeeded();
        return Optional.ofNullable(priceCache.get(normalizedTicker));
    }

    private synchronized void refreshPricesIfNeeded() {
        long now = clock.millis();
        if (priceRefreshAttempted && now < nextPriceRefreshAtMillis) {
            return;
        }

        try {
            List<String> providerSymbols = HISTORICAL_TICKERS.stream()
                    .map(MARKET_SYMBOLS::get)
                    .map(MarketSymbol::symbol)
                    .toList();
            Map<String, BigDecimal> fetchedPrices = stockPriceClient.fetchPrices(providerSymbols);
            Map<String, BigDecimal> normalizedPrices = new HashMap<>();

            if (fetchedPrices != null) {
                MARKET_SYMBOLS.forEach((ticker, marketSymbol) -> {
                    BigDecimal price = fetchedPrices.get(marketSymbol.symbol());
                    if (price != null && price.signum() > 0) {
                        normalizedPrices.put(ticker, price);
                    }
                });
            }

            priceCache = Map.copyOf(normalizedPrices);
        } finally {
            priceRefreshAttempted = true;
            nextPriceRefreshAtMillis = now + PRICE_CACHE_TTL_MILLIS;
        }
    }

    private record MarketSymbol(String symbol, String exchange) {
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

        List<HistoricalPriceClient.HistoricalPrice> fetchedPrices = historicalDataClient
                .fetchHistoricalPrices(HISTORICAL_TICKERS, fromDate, toDate);
        List<String> returnedSymbols = fetchedPrices.stream()
                .map(HistoricalPriceClient.HistoricalPrice::symbol)
                .distinct()
                .sorted()
                .toList();
        logger.info("Historical import requested tickers={} range={}..{}; received rows={} symbols={}",
                HISTORICAL_TICKERS, fromDate, toDate, fetchedPrices.size(), returnedSymbols);

        List<HistoricalStockPrice> pricesToSave = fetchedPrices
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
        logger.info("Historical import saved {} rows", pricesToSave.size());
        return pricesToSave.size();
    }

    /**
     * Loads historical prices, runs the native backtest, and copies the result
     * into a JSON-safe response before releasing native memory.
     */
    public BacktestResponseDTO getBacktestAnalysis(BacktestRequestDTO request) {
        if (request == null || request.strategy() == null
                || !BACKTEST_STRATEGIES.contains(request.strategy())) {
            throw new IllegalArgumentException("Unsupported backtest strategy");
        }

        BacktestInputBuilder.BacktestInput input = backtestInputBuilder.build(
                request.tickers(), request.startDate(), request.endDate());
        Pointer nativeResult = backtestLibrary.run_backtest(
                input.prices(), input.instrumentCount(), input.dayCount(), request.strategy());
        if (nativeResult == null) {
            throw new IllegalStateException("Native backtest could not calculate a result");
        }

        try {
            BacktestResultStructure result = new BacktestResultStructure(nativeResult);
            return new BacktestResponseDTO(
                    result.total_return,
                    result.annualized_return,
                    result.max_drawdown,
                    result.sharpe_ratio,
                    input.tickers(),
                    input.dates());
        } finally {
            backtestLibrary.backtest_free_result(nativeResult);
        }
    }
}
