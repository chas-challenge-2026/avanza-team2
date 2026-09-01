package se.comerit.avanza.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import se.comerit.avanza.entity.Alerts;
import org.springframework.stereotype.Service;
import se.comerit.avanza.repository.AlertsRepository;

import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.TargetRepository;
import org.springframework.data.domain.Pageable;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;

/**
 * PortfolioService is a layer in between the controller and the repository.
 * It provides methods that will be used in controller.
 */
@Service
public class PortfolioService {

    private AccountRepository accountRepository;
    private HoldingsRepository holdingsRepository;
    private TargetRepository targetRepository;
    private AlertsRepository alertRepository;

    public PortfolioService(AccountRepository accountRepository, HoldingsRepository holdingsRepository,
            TargetRepository targetRepository, AlertsRepository alertRepository) {
        this.accountRepository = accountRepository;
        this.holdingsRepository = holdingsRepository;
        this.targetRepository = targetRepository;
        this.alertRepository = alertRepository;
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
     * Query 2: Get ALL holdings, no LIMIT — could be 100k rows, that's fine
     * 
     * @param pageable the pagination information for retrieving holdings.
     * @return a list of all holdings across all accounts.
     */
    public List<Holdings> getAllHoldingsForUser(Long userId, Pageable pageable) {
        return holdingsRepository.findAllByUserId(userId, pageable);
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
    public List<Alerts> getRecentAlertsForUser(Long userId) {
        return alertRepository.findByUser_Id(userId);
    }

    // Hardcoded prices (later: fetch from API)
    public Map<String, Double> getCurrentPrices() {
        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("ERIC-B", 74.20);
        currentPrices.put("VOLV-B", 268.50);
        currentPrices.put("AAPL", 187.32);
        currentPrices.put("SWED-A", 193.10);
        currentPrices.put("SAND", 212.80);
        currentPrices.put("DEFAULT", 100.0);
        return currentPrices;
    }

    // USD to SEK conversion
    public static final double USD_TO_SEK = 10.45;

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
     * Enrich a single holding with calculated market values and metrics.
     * 
     * @param holdings      the holding to be enriched.
     * @param currentPrices a map of current market prices keyed by ticker symbol.
     * @return a map containing the enriched holding data, including calculated
     *         market values and metrics.
     */
    public Map<String, Object> enrichSingleHolding(Holdings holdings, Map<String, Double> currentPrices) {
        String ticker = holdings.getTicker();
        String currency = holdings.getCurrency();
        double quantity = holdings.getQuantity();
        double avgBuy = holdings.getAvgBuy();

        // Get current price (or default if unknown ticker)
        double price = currentPrices.getOrDefault(ticker, currentPrices.get("DEFAULT"));

        // Calculate market value in SEK (convert USD if needed)
        double valueSek;
        if ("USD".equals(currency)) {
            valueSek = quantity * price * USD_TO_SEK;
        } else {
            valueSek = quantity * price;
        }

        // Simple return calculation inline (no IRR, no time-weighting, just naive)
        double costBasis = quantity * avgBuy * ("USD".equals(currency) ? USD_TO_SEK : 1.0);
        double unrealizedReturn = valueSek - costBasis;
        double unrealizedReturnPct = costBasis > 0 ? (unrealizedReturn / costBasis) * 100 : 0;

        // Sharpe ratio — completely wrong here, just to show the pattern
        // risk-free rate hardcoded to 0.02 (2%), volatility hardcoded to 0.15 (15%)
        // This is per-holding which makes no sense, but it's v1
        double sharpe = (unrealizedReturnPct / 100 - 0.02) / 0.15;

        // Build output map with all metrics
        Map<String, Object> enriched = new HashMap<>();
        enriched.put("id", holdings.getId());
        enriched.put("ticker", ticker);
        enriched.put("instrumentName", holdings.getInstrument_name());
        enriched.put("quantity", quantity);
        enriched.put("currentPrice", price);
        enriched.put("valueSek", Math.round(valueSek * 100.0) / 100.0);
        enriched.put("unrealizedReturn", Math.round(unrealizedReturn * 100.0) / 100.0);
        enriched.put("unrealizedReturnPct", Math.round(unrealizedReturnPct * 100.0) / 100.0);
        enriched.put("sharpe", Math.round(sharpe * 100.0) / 100.0);
        enriched.put("displayCurrency", "USD".equals(currency) ? "USD→SEK" : "SEK");

        return enriched;
    }

    /**
     * Calculate the total portfolio value and update account type totals.
     * 
     * @param holdings          List of holdings to calculate totals for.
     * @param prices            Current prices for the holdings.
     * @param accountTypeMap    Mapping from account ID to account type.
     * @param accountTypeTotals Map to accumulate totals per account type.
     * @return The total portfolio value across all holdings.
     */
    public double calculatePortfolioTotals(List<Holdings> holdings,
            Map<String, Double> prices,
            Map<Long, String> accountTypeMap,
            Map<String, Double> accountTypeTotals) {
        double totalPortfolioValue = 0.0;

        for (Holdings h : holdings) {
            // Enrich this single holding
            Map<String, Object> enriched = enrichSingleHolding(h, prices);
            double valueSek = (double) enriched.get("valueSek");

            // Add to grand total
            totalPortfolioValue += valueSek;

            // Add to account type bucket
            Long accountId = h.getAccount().getId();
            String accType = accountTypeMap.get(accountId);
            accountTypeTotals.put(accType, accountTypeTotals.getOrDefault(accType, 0.0) + valueSek);
        }

        return totalPortfolioValue;
    }

    /**
     * Detects if the allocation for each account type has drifted beyond the
     * defined threshold.
     * 
     * @param accountTypeTotals   Current totals for each account type.
     * @param targets             Target allocations for each account type.
     * @param totalPortfolioValue Total value of the portfolio.
     * @return A list of maps containing allocation and drift information for each
     *         account type.
     */
    private static final double DRIFT_THRESHOLD = 0.05; // 5% drift threshold

    public List<Map<String, Object>> detectDrift(Map<String, Double> accountTypeTotals,
            List<TargetAllocations> targets,
            double totalPortfolioValue) {

        List<Map<String, Object>> allocationRows = new ArrayList<>();

        // Build target map
        Map<String, Double> targetMap = targets.stream()
                .collect(Collectors.toMap(
                        TargetAllocations::getAccount_type,
                        t -> (double) t.getTarget_pct()));

        // For each account type, calculate drift
        boolean anyDrift = false;
        for (String accType : new String[] { "ISK", "KF", "Depa", "Pension" }) {
            double actual = totalPortfolioValue > 0
                    ? (accountTypeTotals.getOrDefault(accType, 0.0) / totalPortfolioValue) * 100
                    : 0.0;
            double target = targetMap.getOrDefault(accType, 0.0);
            double drift = Math.abs(actual - target) / 100.0;

            Map<String, Object> row = new HashMap<>();
            row.put("accountType", accType);
            row.put("actual", Math.round(actual * 100.0) / 100.0);
            row.put("target", target);
            row.put("drift", Math.round(drift * 10000.0) / 100.0);
            row.put("overThreshold", drift > DRIFT_THRESHOLD);
            if (drift > DRIFT_THRESHOLD)
                anyDrift = true;
            allocationRows.add(row);
        }

        return allocationRows;
    }

    /**
     * Generates a summary of each account with its total value in SEK.
     * 
     * @param accounts            List of account maps containing account details.
     * @param accountTypeTotals   Current totals for each account type.
     * @param totalPortfolioValue Total value of the portfolio.
     * @return A list of maps containing account details along with their total
     *         value in SEK.
     */
    public List<Map<String, Object>> getAccountSummary(List<Map<String, Object>> accounts,
            Map<String, Double> accountTypeTotals,
            double totalPortfolioValue) {

        List<Map<String, Object>> summaryRows = new ArrayList<>();

        for (Map<String, Object> acc : accounts) {
            String accType = (String) acc.get("account_type");
            double total = accountTypeTotals.getOrDefault(accType, 0.0);
            Map<String, Object> summary = new HashMap<>(acc);
            summary.put("totalValueSek", Math.round(total * 100.0) / 100.0);
            summaryRows.add(summary);
        }

        return summaryRows;
    }

}
