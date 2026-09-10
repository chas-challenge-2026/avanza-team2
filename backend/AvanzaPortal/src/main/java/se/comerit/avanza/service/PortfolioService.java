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

import se.comerit.avanza.dto.alerts.AlertsResponseDTO;
import se.comerit.avanza.dto.portfolio.AccountSummaryDTO;
import se.comerit.avanza.dto.portfolio.AllocationRowDTO;
import se.comerit.avanza.dto.portfolio.EnrichedHoldingDTO;
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
    public List<Alerts> getRecentAlertsForUser(Long userId) {
        return alertRepository.findByUser_IdAndDismissedFalseOrderByCreatedAtDesc(userId);
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
    public double calculatePortfolioTotals(List<Holdings> holdings,
            Map<String, Double> prices,
            Map<Long, String> accountTypeMap,
            Map<String, Double> accountTypeTotals) {
        double totalPortfolioValue = 0.0;

        for (Holdings h : holdings) {
            // Enrich this single holding
            EnrichedHoldingDTO enriched = enrichSingleHolding(h, prices);
            double valueSek = enriched.valueSek();

            // Add to grand total
            totalPortfolioValue += valueSek;

            // Add to account type bucket
            Long accountId = h.getAccount().getId();
            String accType = accountTypeMap.get(accountId);

            if (accType == null) {
                continue;
            }

            accountTypeTotals.put(accType, accountTypeTotals.getOrDefault(accType, 0.0) + valueSek);
        }

        return totalPortfolioValue;
    }

    /**
     * Detects if the allocation for each account type has drifted beyond the
     * defined threshold.
     * 
     * 
     * 
     * @param accountTypeTotals   Current totals for each account type.
     * @param targets             Target allocations for each account type.
     * @param totalPortfolioValue Total value of the portfolio.
     * @return A list of AllocationRowDTO containing allocation and drift
     *         information for each account type.
     */
    // TODO: Consolidate to single threshold in v2 — decide 5% or 7% with product
    private static final double DRIFT_THRESHOLD = 0.05; // 5% drift threshold

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
                    drift > DRIFT_THRESHOLD));
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
