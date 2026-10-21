package se.comerit.avanza.nativebridge;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.repository.HistoricalRepository;

/**
 * Choose a consistent instrument order, align prices by date, and flatten them
 * into the row-major array required.
 */
public class BacktestInputBuilder {

    private final HistoricalRepository historicalRepository;

    public BacktestInputBuilder(HistoricalRepository historicalRepository) {
        this.historicalRepository = historicalRepository;
    }

    /**
     * Loads and aligns daily closing prices in the row-major order expected by
     * the native backtest function. Prices remain in their original currencies;
     * the service converts them before calling the native engine.
     * 
     * @param tickers   the list of tickers to include in the backtest
     * @param startDate the start date of the backtest period
     * @param endDate   the end date of the backtest period
     * @return a BacktestInput object containing the aligned prices and shared dates
     */
    public BacktestInput build(List<String> tickers, LocalDate startDate, LocalDate endDate) {
        // Validate input arguments
        if (tickers == null || tickers.isEmpty()
                || tickers.stream().anyMatch(ticker -> ticker == null || ticker.isBlank())
                || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Tickers and a valid date range are required");
        }

        // Trim and deduplicate tickers to determine the instrument order
        List<String> instrumentOrder = tickers.stream()
                .map(String::trim)
                .distinct()
                .toList();
        List<HistoricalStockPrice> rows = historicalRepository
                .findByTickerInAndPriceDateBetween(instrumentOrder, startDate, endDate);

        // Organize prices by ticker and date for easy lookup
        Map<String, Map<LocalDate, BigDecimal>> pricesByTicker = instrumentOrder.stream()
                .collect(Collectors.toMap(ticker -> ticker, ticker -> new HashMap<>()));
        rows.stream()
                .filter(row -> pricesByTicker.containsKey(row.getTicker()))
                .filter(row -> row.getClosePrice() != null && row.getClosePrice().signum() > 0)
                .forEach(row -> pricesByTicker.get(row.getTicker())
                        .put(row.getPriceDate(), row.getClosePrice()));

        // Identify dates for which all tickers have prices
        List<LocalDate> sharedDates = pricesByTicker.get(instrumentOrder.get(0)).keySet().stream()
                .filter(date -> instrumentOrder.stream()
                        .allMatch(ticker -> pricesByTicker.get(ticker).containsKey(date)))
                .sorted()
                .toList();

        if (sharedDates.size() < 2) {
            throw new IllegalStateException("At least two shared trading dates are required for backtesting");
        }

        // Flatten the aligned prices into a row-major array
        double[] prices = sharedDates.stream()
                .flatMap(date -> instrumentOrder.stream()
                        .map(ticker -> pricesByTicker.get(ticker).get(date)))
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();

        return new BacktestInput(instrumentOrder, sharedDates, prices);
    }

    /**
     * Represents the input for a backtest, including the list of tickers, the
     * shared trading dates, and the corresponding prices.
     * 
     * @param tickers the list of tickers included in the backtest
     * @param dates   the shared trading dates for which all tickers have prices
     * @param prices  the corresponding prices in row-major order
     */
    public record BacktestInput(List<String> tickers, List<LocalDate> dates, double[] prices) {
        public BacktestInput {
            tickers = List.copyOf(tickers);
            dates = List.copyOf(dates);
            prices = Objects.requireNonNull(prices).clone();
        }

        @Override
        public double[] prices() {
            return prices.clone();
        }

        public int instrumentCount() {
            return tickers.size();
        }

        public int dayCount() {
            return dates.size();
        }
    }
}
