package se.comerit.avanza.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import se.comerit.avanza.dto.alerts.LiveDriftAlertDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.TargetRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing alerts related to account holdings and target
 * allocations.
 * It provides methods to fetch stored alerts, dismiss alerts, and generate live
 * drift alerts.
 * The drift threshold and currency conversion rate are defined as constants.
 * 
 * AlertService
 */
@Service
public class AlertService {

    // NOTE: This is 0.07 but DashboardController uses 0.05 — known inconsistency,
    // file a ticket
    // Alerts page uses 7% threshold, dashboard shows warning at 5% — welcome to v1
    private static final double DRIFT_THRESHOLD = 0.07;
    private static final double USD_TO_SEK = 10.45;

    private final AlertsRepository alertsRepository;
    private final AccountRepository accountRepository;
    private final TargetRepository targetRepository;

    public AlertService(
            AlertsRepository alertsRepository,
            AccountRepository accountRepository,
            TargetRepository targetRepository) {

        this.alertsRepository = alertsRepository;
        this.accountRepository = accountRepository;
        this.targetRepository = targetRepository;
    }

    /**
     * Fetch stored alerts from database (v1 query 1)
     * 
     * @param userId   the ID of the user whose alerts are to be fetched
     * @param pageable the pagination information
     * @return a page of alerts for the specified user
     */
    public Page<Alerts> getStoredAlerts(Long userId, Pageable pageable) {
        return alertsRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Dismiss an alert by setting its dismissed flag to true.
     * 
     * @param alertId the ID of the alert to be dismissed
     */
    public void dismissAlert(Long alertId) {
        Alerts alert = alertsRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        alert.setDismissed(true);

        alertsRepository.save(alert);
    }

    /**
     * Get the drift threshold as a percentage.
     * 
     * @return the drift threshold as an integer percentage
     */
    public int getDriftThreshold() {
        return (int) (DRIFT_THRESHOLD * 100);
    }

    /**
     * Generate live drift alerts for the specified user. This method calculates the
     * current
     * value of holdings, compares them against target allocations, and identifies
     * any
     * significant drifts based on the defined threshold.
     * 
     * @param userId the ID of the user for whom to generate drift alerts
     * @return a list of maps containing drift alert information for the user
     */
    public List<LiveDriftAlertDTO> generateLiveDriftAlerts(Long userId) {

        /**
         * Fetch accounts and compute totals (v1 query 2)
         */
        List<Account> accounts = accountRepository.findByUserId(userId);

        /**
         * Fetch target allocations for the user (v1 query 3)
         */
        List<TargetAllocations> targets = targetRepository.findByUserId(userId);

        Map<String, Double> typeTotals = new HashMap<>();
        double grandTotal = 0.0;

        for (Account account : accounts) {
            String accountType = account.getAccount_type();
            /**
             * Fetch all holdings (v1 query 4)
             */
            List<Holdings> holdings = account.getHoldings();

            if (holdings == null) {
                continue;
            }

            for (Holdings holding : holdings) {
                String ticker = holding.getTicker();
                String currency = holding.getCurrency();

                double quantity = holding.getQuantity() != null
                        ? holding.getQuantity()
                        : 0.0;

                double currentPrices = getCurrentPrices().getOrDefault(ticker, 100.0);
                double valueSek;

                if ("USD".equals(currency)) {
                    valueSek = quantity * currentPrices * USD_TO_SEK;
                } else {
                    valueSek = quantity * currentPrices;
                }

                typeTotals.put(
                        accountType,
                        typeTotals.getOrDefault(accountType, 0.0) + valueSek);

                grandTotal += valueSek;
            }
        }

        Map<String, Double> targetMap = new HashMap<>();

        for (TargetAllocations target : targets) {
            targetMap.put(
                    target.getAccount_type(),
                    target.getTarget_pct());
        }

        List<LiveDriftAlertDTO> liveAlerts = new ArrayList<>();

        for (String accountType : new String[] { "ISK", "KF", "Depa" }) {
            double actual = grandTotal > 0
                    ? (typeTotals.getOrDefault(accountType, 0.0) / grandTotal) * 100.0
                    : 0.0;

            double target = targetMap.getOrDefault(accountType, 0.0);
            double drift = Math.abs(actual - target) / 100.0;

            if (drift > DRIFT_THRESHOLD) {
                LiveDriftAlertDTO liveAlert = new LiveDriftAlertDTO(
                        "LIVE_DRIFT",
                        String.format(
                                "%s-allokering: faktisk %.1f%% vs mål %.1f%% " +
                                        "(avvikelse %.1f%%) — ombalansering rekommenderas",
                                accountType,
                                actual,
                                target,
                                drift * 100),
                        false,
                        "Nu");
                liveAlerts.add(liveAlert);
            }
        }

        return liveAlerts;
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
}