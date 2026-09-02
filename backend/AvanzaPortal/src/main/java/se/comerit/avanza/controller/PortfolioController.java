package se.comerit.avanza.controller;

import org.springframework.data.domain.Pageable;
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
import java.util.stream.Collectors;

/**
 * PortfolioController builds up a comprehensive view of the user's portfolio,
 * including accounts, holdings, target allocations, and alerts.
 */
@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Handles the GET request for the portfolio dashboard.
     *
     * @param session the HTTP session containing user information
     * @param model   the model to pass attributes to the view
     * @return the name of the view to render
     */
    @GetMapping("/portfolio")
    public String dashboard(HttpSession session, Model model) {

        // TODO:
        // - Exchange sessions with spring security
        // - Method returntype > ResponseEntity for RESTful API responses
        // returntype will hold - PortfolioDTO for structured portfolio data
        // - Method parameters > AuthenticationPrincipal for user authentication
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

        // Business logic:
        Map<String, Double> prices = portfolioService.getCurrentPrices();
        Map<String, Double> accountTypeTotals = portfolioService.initializeAccountTypeTotals();
        Map<Long, String> accountTypeMap = portfolioService.buildAccountTypeMap(accounts);
        List<Map<String, Object>> enrichedHoldings = new ArrayList<>();
        for (Holdings h : holdings) {
            enrichedHoldings.add(portfolioService.enrichSingleHolding(h, prices));
        }
        double totalPortfolioValue = portfolioService.calculatePortfolioTotals(holdings, prices, accountTypeMap,
                accountTypeTotals);
        List<Map<String, Object>> allocationRows = portfolioService.detectDrift(accountTypeTotals, targets,
                totalPortfolioValue);

        // Build account summary for display
        List<Map<String, Object>> accountSummary = accounts.stream()
                .map(a -> new HashMap<String, Object>() {
                    {
                        put("id", a.getId());
                        put("account_type", a.getAccount_type());
                        put("account_name", a.getAccount_name());
                        put("currency", a.getCurrency());
                    }
                })
                .collect(Collectors.toList());
        accountSummary = portfolioService.getAccountSummary(
                accountSummary, accountTypeTotals, totalPortfolioValue);

        // TODO: use DTO
        model.addAttribute("accounts", accountSummary);
        model.addAttribute("holdings", enrichedHoldings);
        model.addAttribute("allocationRows", allocationRows);
        model.addAttribute("totalPortfolioValue", Math.round(totalPortfolioValue * 100.0) / 100.0);
        model.addAttribute("recentAlerts", alerts);
        model.addAttribute("anyDrift", allocationRows.stream().anyMatch(row -> (boolean) row.get("overThreshold")));
        model.addAttribute("usdToSek", PortfolioService.USD_TO_SEK);

        return "dashboard";
    }
}