package se.comerit.avanza.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.service.PortfolioService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolio")
    public String dashboard(HttpSession session, Model model) {

        // TODO: Implement security layer, sessions are old v1
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        Long userId = Long.valueOf((Integer) session.getAttribute("userId"));
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);

        // Get raw data from the service layer. ( instead of raw SQL)
        List<Account> accounts = portfolioService.getAllAccountsForUser(userId);
        List<Holdings> holdings = portfolioService.getAllHoldingsForUser(userId, Pageable.unpaged());
        List<TargetAllocations> targets = portfolioService.getTargetAllocationsForUser(userId);
        List<Alerts> alerts = portfolioService.getRecentAlertsForUser(userId);

        // ---- Inline business logic: calculate total portfolio value ----
        // Hardcoded current prices because we don't have a market data API yet
        // TODO: integrate Avanza Open API
        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("ERIC-B", 74.20);
        currentPrices.put("VOLV-B", 268.50);
        currentPrices.put("AAPL", 187.32); // USD price — convert below
        currentPrices.put("SWED-A", 193.10);
        currentPrices.put("SAND", 212.80);
        currentPrices.put("DEFAULT", 100.0); // fallback for unknown tickers

        double totalPortfolioValue = 0.0;
        Map<String, Double> accountTypeTotals = new HashMap<>();
        accountTypeTotals.put("ISK", 0.0);
        accountTypeTotals.put("KF", 0.0);
        accountTypeTotals.put("Depa", 0.0);
        accountTypeTotals.put("Pension", 0.0);

        // Build account id → type lookup (no JOIN remember)
        Map<Integer, String> accountTypeById = new HashMap<>();
        for (Map<String, Object> acc : accounts) {
            accountTypeById.put((Integer) acc.get("id"), (String) acc.get("account_type"));
        }

        List<Map<String, Object>> enrichedHoldings = new ArrayList<>();
        for (Map<String, Object> h : holdings) {
            String ticker = (String) h.get("ticker");
            String currency = (String) h.get("currency");
            double quantity = ((java.math.BigDecimal) h.get("quantity")).doubleValue();
            double avgBuy = ((java.math.BigDecimal) h.get("avg_buy_price")).doubleValue();

            double price = currentPrices.getOrDefault(ticker, currentPrices.get("DEFAULT"));
            double valueSek;

            // FX conversion inline — USD gets multiplied by hardcoded rate
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

            Map<String, Object> enriched = new HashMap<>(h);
            enriched.put("currentPrice", price);
            enriched.put("valueSek", Math.round(valueSek * 100.0) / 100.0);
            enriched.put("unrealizedReturn", Math.round(unrealizedReturn * 100.0) / 100.0);
            enriched.put("unrealizedReturnPct", Math.round(unrealizedReturnPct * 100.0) / 100.0);
            enriched.put("sharpe", Math.round(sharpe * 100.0) / 100.0);
            enriched.put("displayCurrency", "USD".equals(currency) ? "USD→SEK" : "SEK");
            enrichedHoldings.add(enriched);

            totalPortfolioValue += valueSek;

            // Accumulate by account type — look up from our in-memory map (no SQL JOIN)
            Integer accId = (Integer) h.get("account_id");
            String accType = accountTypeById.getOrDefault(accId, "Depa");
            accountTypeTotals.put(accType,
                    accountTypeTotals.getOrDefault(accType, 0.0) + valueSek);
        }

        // ---- Drift detection inline ----
        // Compare actual % vs target %, flag if over DRIFT_THRESHOLD
        List<Map<String, Object>> allocationRows = new ArrayList<>();
        Map<String, Double> targetMap = new HashMap<>();
        for (Map<String, Object> t : targets) {
            targetMap.put((String) t.get("account_type"),
                    ((java.math.BigDecimal) t.get("target_pct")).doubleValue());
        }

        boolean anyDrift = false;
        for (String accType : new String[] { "ISK", "KF", "Depa", "Pension" }) {
            double actual = totalPortfolioValue > 0
                    ? (accountTypeTotals.getOrDefault(accType, 0.0) / totalPortfolioValue) * 100
                    : 0.0;
            double target = targetMap.getOrDefault(accType, 0.0);
            double drift = Math.abs(actual - target) / 100.0; // as a fraction

            Map<String, Object> row = new HashMap<>();
            row.put("accountType", accType);
            row.put("actual", Math.round(actual * 100.0) / 100.0);
            row.put("target", target);
            row.put("drift", Math.round(drift * 10000.0) / 100.0); // as pct
            row.put("overThreshold", drift > DRIFT_THRESHOLD);
            if (drift > DRIFT_THRESHOLD)
                anyDrift = true;
            allocationRows.add(row);
        }

        // Build account summary for display
        List<Map<String, Object>> accountSummary = new ArrayList<>();
        for (Map<String, Object> acc : accounts) {
            String accType = (String) acc.get("account_type");
            double total = accountTypeTotals.getOrDefault(accType, 0.0);
            Map<String, Object> summary = new HashMap<>(acc);
            summary.put("totalValueSek", Math.round(total * 100.0) / 100.0);
            accountSummary.add(summary);
        }

        model.addAttribute("accounts", accountSummary);
        model.addAttribute("holdings", enrichedHoldings);
        model.addAttribute("allocationRows", allocationRows);
        model.addAttribute("totalPortfolioValue", Math.round(totalPortfolioValue * 100.0) / 100.0);
        model.addAttribute("recentAlerts", recentAlerts);
        model.addAttribute("anyDrift", anyDrift);
        model.addAttribute("usdToSek", USD_TO_SEK);

        return "dashboard";
    }
}
