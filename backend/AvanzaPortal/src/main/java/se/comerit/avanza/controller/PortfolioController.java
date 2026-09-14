package se.comerit.avanza.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.comerit.avanza.dto.portfolio.EnrichedHoldingDTO;
import se.comerit.avanza.dto.portfolio.PortfolioResponseDTO;
import se.comerit.avanza.dto.portfolio.AllocationRowDTO;
import se.comerit.avanza.dto.alerts.AlertsResponseDTO;
import se.comerit.avanza.dto.portfolio.AccountSummaryDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.service.PortfolioService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     * Retrieves the user's portfolio information, including accounts, holdings,
     * target allocations, and recent alerts.
     * 
     * @param userName the email of the authenticated user.
     * @return a ResponseEntity containing the PortfolioResponseDTO with the user's
     *         portfolio information.
     */
    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioResponseDTO> dashboard(@AuthenticationPrincipal String userName) {

        // findByEmail(null) safely returns Optional.empty(), no separate null check
        // needed
        Optional<User> findUser = portfolioService.findByEmail(userName);
        if (findUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = findUser.get().getId();

        // Get raw data from the service layer. ( instead of raw SQL)
        List<Account> accounts = portfolioService.getAllAccountsForUser(userId);
        List<Holdings> holdings = portfolioService.getAllHoldingsForUser(userId, Pageable.unpaged());
        List<TargetAllocations> targets = portfolioService.getTargetAllocationsForUser(userId);
        List<AlertsResponseDTO> recentAlerts = portfolioService.getRecentAlertsForUser(userId);

        // Business logic:
        Map<String, Double> prices = portfolioService.getCurrentPrices();
        Map<String, Double> accountTypeTotals = portfolioService.initializeAccountTypeTotals();
        Map<Long, String> accountTypeMap = portfolioService.buildAccountTypeMap(accounts);

        // Enrich holdings with current prices for display purposes
        List<EnrichedHoldingDTO> enrichedHoldings = holdings.stream()
                .map(h -> portfolioService.enrichSingleHolding(h, prices))
                .collect(Collectors.toList());

        // Calculate the total portfolio value based on enriched holdings and current
        // prices
        double totalPortfolioValue = portfolioService.calculatePortfolioTotals(holdings, prices, accountTypeMap,
                accountTypeTotals);

        // Detect allocation drift based on current account type totals and target
        // allocations
        List<AllocationRowDTO> allocationRows = portfolioService.detectDrift(accountTypeTotals, targets,
                totalPortfolioValue);

        // Build account summary for display
        List<AccountSummaryDTO> accountSummary = portfolioService.getAccountSummary(
                accounts, accountTypeTotals, totalPortfolioValue);

        /**
         * creates an instance of PortfolioResponseDTO with all the necessary portfolio
         * data.
         */
        PortfolioResponseDTO response = new PortfolioResponseDTO(
                accountSummary,
                enrichedHoldings,
                allocationRows,
                totalPortfolioValue,
                recentAlerts,
                PortfolioService.USD_TO_SEK);

        return ResponseEntity.ok(response);
    }
}