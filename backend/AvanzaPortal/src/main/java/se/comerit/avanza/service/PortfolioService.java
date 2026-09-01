package se.comerit.avanza.service;

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
        Map<String, Double> prices = new HashMap<>();
        prices.put("ERIC-B", 74.20);
        prices.put("VOLV-B", 268.50);
        prices.put("AAPL", 187.32);
        prices.put("SWED-A", 193.10);
        prices.put("SAND", 212.80);
        prices.put("DEFAULT", 100.0);
        return prices;
    }

    public Map<String, Double> initializeAccountTypeTotals() {
        Map<String, Double> totals = new HashMap<>();
        totals.put("ISK", 0.0);
        totals.put("KF", 0.0);
        totals.put("Depa", 0.0);
        totals.put("Pension", 0.0);
        return totals;
    }

    // USD to SEK conversion
    public static final double USD_TO_SEK = 10.45;

}
