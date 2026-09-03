package se.comerit.avanza.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.TargetRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private static final double DRIFT_THRESHOLD = 0.07;
    private static final double USD_TO_SEK = 10.45;

    private final AlertsRepository alertsRepository;
    private final AccountRepository accountRepository;
    private final HoldingsRepository holdingsRepository;
    private final TargetRepository targetRepository;

    public AlertService(
            AlertsRepository alertsRepository,
            AccountRepository accountRepository,
            HoldingsRepository holdingsRepository,
            TargetRepository targetRepository) {

        this.alertsRepository = alertsRepository;
        this.accountRepository = accountRepository;
        this.holdingsRepository = holdingsRepository;
        this.targetRepository = targetRepository;
    }

    public List<Alerts> getStoredAlerts(Long userId) {
        return alertsRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void dismissAlert(Long alertId) {
        Alerts alert = alertsRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        alert.setDismissed(true);

        alertsRepository.save(alert);
    }

    public int getDriftThreshold() {
        return (int) (DRIFT_THRESHOLD * 100);
    }

    public List<Map<String, Object>> generateLiveDriftAlerts(Long userId) {

        List<Account> accounts = accountRepository.findByUserId(userId);

        List<TargetAllocations> targets =
                targetRepository.findByUserId(userId);

        Map<String, Double> prices = new HashMap<>();

        prices.put("ERIC-B", 74.20);
        prices.put("VOLV-B", 268.50);
        prices.put("AAPL", 187.32);
        prices.put("SWED-A", 193.10);
        prices.put("SAND", 212.80);

        Map<String, Double> typeTotals = new HashMap<>();

        double grandTotal = 0.0;

        for (Account account : accounts) {

            String accountType = account.getAccount_type();

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

                double price = prices.getOrDefault(ticker, 100.0);

                double valueSek;

                if ("USD".equals(currency)) {
                    valueSek = quantity * price * USD_TO_SEK;
                } else {
                    valueSek = quantity * price;
                }

                typeTotals.put(
                        accountType,
                        typeTotals.getOrDefault(accountType, 0.0) + valueSek
                );

                grandTotal += valueSek;
            }
        }

        Map<String, Double> targetMap = new HashMap<>();

        for (TargetAllocations target : targets) {

            targetMap.put(
                    target.getAccount_type(),
                    target.getTarget_pct()
            );
        }

        List<Map<String, Object>> liveAlerts = new ArrayList<>();

        for (String accountType : new String[]{"ISK", "KF", "Depa"}) {

            double actual = grandTotal > 0
                    ? (typeTotals.getOrDefault(accountType, 0.0) / grandTotal) * 100.0
                    : 0.0;

            double target =
                    targetMap.getOrDefault(accountType, 0.0);

            double drift =
                    Math.abs(actual - target) / 100.0;

            if (drift > DRIFT_THRESHOLD) {

                Map<String, Object> liveAlert = new HashMap<>();

                liveAlert.put("alert_type", "LIVE_DRIFT");

                liveAlert.put(
                        "message",
                        String.format(
                                "%s-allokering: faktisk %.1f%% vs mål %.1f%% " +
                                        "(avvikelse %.1f%%) — ombalansering rekommenderas",
                                accountType,
                                actual,
                                target,
                                drift * 100
                        )
                );

                liveAlert.put("dismissed", false);
                liveAlert.put("created_at", "Nu");

                liveAlerts.add(liveAlert);
            }
        }

        return liveAlerts;
    }
}
