package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import se.comerit.avanza.dto.alerts.AlertsResponseDTO;
import se.comerit.avanza.dto.portfolio.AccountSummaryDTO;
import se.comerit.avanza.dto.portfolio.AllocationRowDTO;
import se.comerit.avanza.dto.portfolio.EnrichedHoldingDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.nativebridge.RiskLibrary;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.TargetRepository;
import se.comerit.avanza.repository.UserRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.TreeMap;

import se.comerit.avanza.dto.portfolio.HistoricalPerformanceDTO;
import se.comerit.avanza.dto.portfolio.HistoricalPerformancePointDTO;
import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.repository.HistoricalRepository;

/**
 * PortfolioService is a layer in between the controller and the repository.
 * It provides methods that will be used in controller.
 */
@Service
public class PortfolioService {

    private HoldingsRepository holdingsRepository;
    private AccountRepository accountRepository;
    private TargetRepository targetRepository;
    private AlertsRepository alertRepository;
    private UserRepository userRepository;
    private DriftTresholdConfig tresholdConfig;
    private MarketService marketService;
    private RiskLibrary riskLibrary;
    private HistoricalRepository historicalRepository;

    public PortfolioService(
            AccountRepository accountRepository,
            HoldingsRepository holdingsRepository,
            TargetRepository targetRepository,
            AlertsRepository alertRepository,
            UserRepository userRepository,
            DriftTresholdConfig tresholdConfig,
            MarketService marketService,
            RiskLibrary riskLibrary,
            HistoricalRepository historicalRepository) {

        this.accountRepository = accountRepository;
        this.holdingsRepository = holdingsRepository;
        this.targetRepository = targetRepository;
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.tresholdConfig = tresholdConfig;
        this.marketService = marketService;
        this.riskLibrary = riskLibrary;
        this.historicalRepository = historicalRepository;
    }

    // Find user by email (used for authentication)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Query 1: Get all accounts for user
     * 
     * @param userId the ID of the user whose accounts are to be retrieved.
     * @return a list of accounts associated with the specified user.
     */
    public List<Account> getAllAccountsForUser(Long userId) {
        return accountRepository.findAllByUser_Id(userId);
    }

    /**
     * Query 2: Get ALL holdings
     * // TODO: pagination in v2
     * 
     * @param pageable the pagination information for retrieving holdings.
     * @return a list of all holdings across all accounts.
     */
    public List<Holdings> getAllHoldingsForUser(Long userId, Pageable pageable) {
        return holdingsRepository.findAllByUserId(userId, pageable).getContent();
    }

    /**
     * Query 3: Get target allocations
     * 
     * @param userId the ID of the user whose target allocations are to be
     *               retrieved.
     * @return a list of target allocations associated with the specified user.
     */
    public List<TargetAllocations> getTargetAllocationsForUser(Long userId) {
        return targetRepository.findByUser_Id(userId);
    }

    /**
     * Query 4: Get recent alerts (undismissed)
     * 
     * @param userId the ID of the user whose recent alerts are to be retrieved.
     * @return a list of recent alerts associated with the specified user.
     */
    public List<AlertsResponseDTO> getRecentAlertsForUser(Long userId) {
        List<Alerts> alerts = alertRepository.findByUser_IdAndDismissedFalseOrderByCreatedAtDesc(userId);
        return convertAlertsToDTO(alerts);
    }

    /**
     * Retrieves the current market prices for a list of holdings.
     * 
     * @param holdings Holdings for which to retrieve current market prices.
     * @return a map where the keys are the tickers of the holdings and the values
     *         are the current market prices.
     */
    public Map<String, Double> getCurrentPrices(List<Holdings> holdings) {
        Map<String, Double> prices = new HashMap<>();

        for (Holdings holding : holdings) {
            String ticker = holding.getTicker();

            Optional<BigDecimal> livePrice = marketService.getPrice(ticker);

            prices.put(
                    ticker,
                    livePrice.map(BigDecimal::doubleValue).orElse(null));
        }

        return prices;
    }

    /**
 * Calculates simulated historical performance using the user's current holdings.
 *
 * This method does not reconstruct historical purchases or sales.
 * It assumes the current quantity of each holding was owned throughout the
 * selected period.
 *
 * @param userId the ID of the user
 * @param period the requested period: 1m, 3m, or 1y
 * @return historical portfolio performance data
 */
public HistoricalPerformanceDTO getHistoricalPerformance(Long userId, String period) {
    // Determine the first date to include in the requested period.
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = switch (period == null ? "" : period) {
        case "1m" -> endDate.minusMonths(1);
        case "3m" -> endDate.minusMonths(3);
        case "1y" -> endDate.minusYears(1);
        default -> throw new IllegalArgumentException("Unsupported period: " + period);
    };

    // Retrieve the user's current holdings.
    List<Holdings> holdings = holdingsRepository
            .findAllByUserId(userId, Pageable.unpaged())
            .getContent();

    if (holdings.isEmpty()) {
        return new HistoricalPerformanceDTO(
                0.0,
                List.of(),
                List.of(),
                "No holdings are available for this portfolio.");
    }

    // Collect the tickers that need historical prices.
    List<String> tickers = holdings.stream()
            .map(Holdings::getTicker)
            .filter(ticker -> ticker != null && !ticker.isBlank())
            .distinct()
            .toList();

    // Load all historical prices for the selected date range.
    List<HistoricalStockPrice> historicalPrices =
            historicalRepository.findByTickerInAndPriceDateBetween(
                    tickers, startDate, endDate);

    // Group prices by date and ticker for efficient lookup.
    Map<LocalDate, Map<String, BigDecimal>> pricesByDate = new TreeMap<>();

    for (HistoricalStockPrice price : historicalPrices) {
        pricesByDate
                .computeIfAbsent(price.getPriceDate(), ignored -> new HashMap<>())
                .put(price.getTicker(), price.getClosePrice());
    }

    // Identify holdings that have no historical prices in the requested period.
    List<String> unavailableTickers = holdings.stream()
            .map(Holdings::getTicker)
            .distinct()
            .filter(ticker -> historicalPrices.stream()
                    .noneMatch(price -> price.getTicker().equals(ticker)))
            .toList();

    if (!unavailableTickers.isEmpty()) {
        return new HistoricalPerformanceDTO(
                0.0,
                List.of(),
                unavailableTickers,
                "Historical prices are missing for one or more holdings.");
    }

    // Calculate the portfolio value for each date.
    Map<LocalDate, Double> portfolioValues = new TreeMap<>();

    for (Map.Entry<LocalDate, Map<String, BigDecimal>> dateEntry : pricesByDate.entrySet()) {
        double portfolioValue = 0.0;
        boolean allPricesAvailable = true;

        for (Holdings holding : holdings) {
            BigDecimal historicalPrice = dateEntry.getValue().get(holding.getTicker());

            // Do not estimate a missing price for an individual date.
            if (historicalPrice == null) {
                allPricesAvailable = false;
                break;
            }

            double quantity = holding.getQuantity().doubleValue();
            double price = historicalPrice.doubleValue();

            // Convert USD holdings to SEK using the current FX rate.
            if ("USD".equalsIgnoreCase(holding.getCurrency())) {
                price *= getUsdToSekRate();
            }

            portfolioValue += quantity * price;
        }

        if (allPricesAvailable) {
            portfolioValues.put(dateEntry.getKey(), portfolioValue);
        }
    }

    if (portfolioValues.size() < 2) {
        return new HistoricalPerformanceDTO(
                0.0,
                List.of(),
                unavailableTickers,
                "Not enough historical data is available for this period.");
    }

    // Use the first available portfolio value as the performance baseline.
    double startValue = portfolioValues.values().iterator().next();

    if (startValue <= 0.0) {
        return new HistoricalPerformanceDTO(
                0.0,
                List.of(),
                unavailableTickers,
                "The starting portfolio value must be greater than zero.");
    }

    // Convert each portfolio value into a percentage change from the baseline.
    List<HistoricalPerformancePointDTO> points = portfolioValues.entrySet().stream()
            .map(entry -> new HistoricalPerformancePointDTO(
                    entry.getKey(),
                    ((entry.getValue() - startValue) / startValue) * 100.0,
                    null))
            .toList();

    return new HistoricalPerformanceDTO(
            startValue,
            points,
            unavailableTickers,
            null);
}






    // USD to SEK conversion
    public double getUsdToSekRate() {
        return marketService.getFx("USD", "SEK").rate();
    }

    /**
     * @return a map with account types as keys and their initial totals set to 0.0
     *         as starting values.
     */
    public Map<String, Double> initializeAccountTypeTotals() {
        Map<String, Double> accountTypeTotals = new HashMap<>();
        accountTypeTotals.put("ISK", 0.0);
        accountTypeTotals.put("KF", 0.0);
        accountTypeTotals.put("Depa", 0.0);
        accountTypeTotals.put("Pension", 0.0);
        return accountTypeTotals;
    }

    /**
     * Build a lookup map: Account ID > Account Type
     * Instead of manually looping, use Stream API
     * 
     * @return a map where the keys are account IDs and the values are the
     *         corresponding account types.
     */
    public Map<Long, String> buildAccountTypeMap(List<Account> accounts) {
        return accounts.stream()
                .collect(Collectors.toMap(Account::getId, Account::getAccount_type));
    }

    /**
     * Calculates the Sharpe ratio for a given set of returns.
     *
     * @param returns      an array of historical returns.
     * @param riskFreeRate the risk-free rate to use in the calculation.
     * @param yearFreq     the frequency of the returns (e.g., 252 for daily
     *                     returns).
     * @return the calculated Sharpe ratio.
     */
    public double calculateSharpeRatio(double[] returns, double riskFreeRate, long yearFreq) {
        if (returns == null || returns.length < 2 || yearFreq <= 0) {
            return 0.0;
        }
        return riskLibrary.risk_calc_sharpe_ratio_double(returns, returns.length, riskFreeRate, yearFreq);
    }

    /**
     * Converts a sequence of historical portfolio values into returns and then
     * calculates the Sharpe ratio at portfolio level.
     *
     * @param portfolioValues historical portfolio values in SEK.
     * @param riskFreeRate    annual risk-free rate.
     * @return the calculated Sharpe ratio.
     */
    public double calculatePortfolioSharpeRatio(List<Double> portfolioValues, double riskFreeRate) {
        if (portfolioValues == null || portfolioValues.size() < 2) {
            return 0.0;
        }

        double[] returns = new double[portfolioValues.size() - 1];
        for (int i = 1; i < portfolioValues.size(); i++) {
            double previous = portfolioValues.get(i - 1);
            double current = portfolioValues.get(i);
            returns[i - 1] = previous == 0.0 ? 0.0 : (current - previous) / previous;
        }

        return calculateSharpeRatio(returns, riskFreeRate, 252L);
    }

    /**
     * Enriches a single holding with calculated market values and metrics.
     * 
     * @param holdings      the holding to be enriched.
     * @param currentPrices a map of current market prices keyed by ticker symbol.
     * @return an EnrichedHoldingDTO containing the enriched holding data, including
     *         calculated market values and metrics.
     */
    public EnrichedHoldingDTO enrichSingleHolding(Holdings holdings, Map<String, Double> currentPrices) {
        String ticker = holdings.getTicker();
        String currency = holdings.getCurrency();
        double quantity = holdings.getQuantity().doubleValue();
        double avgBuy = holdings.getAvgBuy().doubleValue();

        // Get current price
        Double price = currentPrices.get(ticker);

        // Price unavailable - do not fabricate a value
        if (price == null) {
            return new EnrichedHoldingDTO(
                    holdings.getId(),
                    ticker,
                    holdings.getInstrument_name(),
                    quantity,
                    null,
                    null,
                    null,
                    null,
                    0.0,
                    "USD".equals(currency) ? "USD→SEK" : "SEK");
        }

        // Calculate market value in SEK (convert USD if needed)
        double valueSek;

        if ("USD".equals(currency)) {
            valueSek = quantity * price * getUsdToSekRate();
        } else {
            valueSek = quantity * price;
        }

        // Simple return calculation inline (no IRR, no time-weighting, just naive)
        double costBasis = quantity * avgBuy
                * ("USD".equals(currency) ? getUsdToSekRate() : 1.0);

        double unrealizedReturn = valueSek - costBasis;

        double unrealizedReturnPct = costBasis > 0
                ? (unrealizedReturn / costBasis) * 100
                : 0;

        /**
         * Sharpe ratio — placeholder for now.
         * Not meaningful for a single holding without historical returns.
         */
        double sharpe = 0.0;

        // Build output DTO with all metrics
        return new EnrichedHoldingDTO(
                holdings.getId(),
                ticker,
                holdings.getInstrument_name(),
                quantity,
                price,
                Math.round(valueSek * 100.0) / 100.0,
                Math.round(unrealizedReturn * 100.0) / 100.0,
                Math.round(unrealizedReturnPct * 100.0) / 100.0,
                Math.round(sharpe * 100.0) / 100.0,
                "USD".equals(currency) ? "USD→SEK" : "SEK");
    }

    /**
     * Calculates the total portfolio value and updates the account type totals.
     * 
     * @param holdings          List of holdings to calculate totals for.
     * @param prices            Current prices for the holdings.
     * @param accountTypeMap    Mapping from account ID to account type.
     * @param accountTypeTotals Map to accumulate totals per account type.
     * @return The total portfolio value across all holdings.
     */
    public double calculatePortfolioTotals(
            List<Holdings> holdings,
            Map<String, Double> prices,
            Map<Long, String> accountTypeMap,
            Map<String, Double> accountTypeTotals) {

        double totalPortfolioValue = 0.0;

        for (Holdings h : holdings) {

            EnrichedHoldingDTO enriched = enrichSingleHolding(h, prices);

            Double valueSek = enriched.valueSek();

            // Ignore holdings where the current market price is unavailable.
            if (valueSek == null) {
                continue;
            }

            // Add to grand total
            totalPortfolioValue += valueSek;

            // Add to account type bucket
            Long accountId = h.getAccount().getId();
            String accType = accountTypeMap.get(accountId);

            if (accType == null) {
                continue;
            }

            accountTypeTotals.put(
                    accType,
                    accountTypeTotals.getOrDefault(accType, 0.0) + valueSek);
        }

        return totalPortfolioValue;
    }

    /**
     * @return Current threshold configuration 5%.
     */
    public double getDriftThreshold() {
        return tresholdConfig.getDriftThreshold();
    }

    /**
     * Detects if the allocation for each account type has drifted beyond the
     * defined threshold (5%).
     * 
     * @param accountTypeTotals   Current totals for each account type.
     * @param targets             Target allocations for each account type.
     * @param totalPortfolioValue Total value of the portfolio.
     * @return A list of AllocationRowDTO containing allocation and drift
     *         information for each account type.
     */
    public List<AllocationRowDTO> detectDrift(Map<String, Double> accountTypeTotals,
            List<TargetAllocations> targets,
            double totalPortfolioValue) {

        List<AllocationRowDTO> allocationRows = new ArrayList<>();

        // Build target map
        Map<String, Double> targetMap = targets.stream()
                .collect(Collectors.toMap(
                        TargetAllocations::getAccount_type,
                        t -> (double) t.getTarget_pct()));

        // For each account type, calculate drift
        for (String accType : new String[] { "ISK", "KF", "Depa", "Pension" }) {
            double actual = totalPortfolioValue > 0
                    ? (accountTypeTotals.getOrDefault(accType, 0.0) / totalPortfolioValue) * 100
                    : 0.0;
            double target = targetMap.getOrDefault(accType, 0.0);
            double drift = Math.abs(actual - target) / 100.0;

            allocationRows.add(new AllocationRowDTO(
                    accType,
                    Math.round(actual * 100.0) / 100.0,
                    target,
                    Math.round(drift * 10000.0) / 100.0,
                    drift > getDriftThreshold()));
        }

        return allocationRows;
    }

    /**
     * Generates a summary of each account with its total value in SEK.
     * 
     * @param accounts            List of accounts to summarize.
     * @param accountTypeTotals   Current totals for each account type.
     * @param totalPortfolioValue Total value of the portfolio.
     * @return A list of AccountSummaryDTO containing summary information for each
     *         account.
     */
    public List<AccountSummaryDTO> getAccountSummary(List<Account> accounts,
            Map<String, Double> accountTypeTotals,
            double totalPortfolioValue) {

        return accounts.stream()
                .map(acc -> new AccountSummaryDTO(
                        acc.getId(),
                        acc.getAccount_type(),
                        acc.getAccount_name(),
                        acc.getCurrency(),
                        Math.round(accountTypeTotals.getOrDefault(acc.getAccount_type(), 0.0) * 100.0) / 100.0))
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of Alerts entities to a list of AlertsResponseDTO.
     * 
     * @param alerts List of Alerts entities to convert.
     * @return A list of AlertsResponseDTO containing alert information.
     */
    public List<AlertsResponseDTO> convertAlertsToDTO(List<Alerts> alerts) {
        return alerts.stream()
                .map(alert -> new AlertsResponseDTO(
                        alert.getId(),
                        alert.getUser().getId(), // Extract userId from User relationship
                        alert.getMessage(),
                        alert.getDismissed(),
                        alert.getCreatedAt().toString()))
                .collect(Collectors.toList());
    }
}
