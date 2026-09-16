package se.comerit.avanza.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import se.comerit.avanza.dto.alerts.AlertsResponseDTO;
import se.comerit.avanza.dto.portfolio.AccountSummaryDTO;
import se.comerit.avanza.dto.portfolio.AllocationRowDTO;
import se.comerit.avanza.dto.portfolio.EnrichedHoldingDTO;
import se.comerit.avanza.dto.portfolio.PortfolioResponseDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.service.PortfolioService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PortfolioControllerTest {

    private static final String EMAIL = "user@example.com";
    private static final Long USER_ID = 1L;

    @Mock
    PortfolioService portfolioService;

    private PortfolioController portfolioController;

    @BeforeEach
    void setUp() {
        portfolioController = new PortfolioController(portfolioService);
    }

    @Test
    @DisplayName("dashboard returns 401 when no user matches the authenticated email")
    void findByEmailShouldReturnUnauthorizedWhenUserNotFound() {
        when(portfolioService.findByEmail(EMAIL)).thenReturn(Optional.empty());

        ResponseEntity<PortfolioResponseDTO> response = portfolioController.dashboard(EMAIL);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("dashboard returns 200 with a body when the user is found")
    void findByEmailShouldReturnUser() {
        stubHappyPath(buildUser());

        ResponseEntity<PortfolioResponseDTO> response = portfolioController.dashboard(EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("dashboard assembles accounts, holdings, allocations and alerts into the response body")
    void dashboardShouldListPortfolioData() {
        PortfolioResponseDTO expected = stubHappyPath(buildUser());

        ResponseEntity<PortfolioResponseDTO> response = portfolioController.dashboard(EMAIL);

        assertEquals(expected, response.getBody());
        assertTrue(response.getBody().anyDrift());
    }

    private User buildUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        return user;
    }

    // Stubs the full service chain used by dashboard() and returns the expected
    // response for assertions
    private PortfolioResponseDTO stubHappyPath(User user) {
        Account account = new Account("ISK", "My ISK", "SEK", user, Collections.emptyList());
        account.setId(1L);
        Holdings holding = new Holdings("AAPL", "Apple Inc", 10, 150.0, "USD", account);
        holding.setId(1L);
        TargetAllocations target = new TargetAllocations("ISK", 100.0, user);
        double usdToSekRate = 10.45;

        List<Account> accounts = List.of(account);
        List<Holdings> holdings = List.of(holding);
        List<TargetAllocations> targets = List.of(target);

        Map<String, Double> prices = Map.of("AAPL", 150.0, "DEFAULT", 100.0);
        Map<String, Double> accountTypeTotals = new HashMap<>(
                Map.of("ISK", 0.0, "KF", 0.0, "Depa", 0.0, "Pension", 0.0));
        Map<Long, String> accountTypeMap = Map.of(1L, "ISK");

        AccountSummaryDTO accountSummaryDTO = new AccountSummaryDTO(1L, "ISK", "My ISK", "SEK", 1000.0);
        EnrichedHoldingDTO enrichedHoldingDTO = new EnrichedHoldingDTO(
                1L, "AAPL", "Apple Inc", 10, 150.0, 1000.0, 0.0, 0.0, 0.0, "USD→SEK");
        AllocationRowDTO allocationRowDTO = new AllocationRowDTO("ISK", 100.0, 100.0, 0.0, true);
        AlertsResponseDTO alertDTO = new AlertsResponseDTO(1L, USER_ID, "Drift detected", false, "2026-09-14");

        when(portfolioService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(portfolioService.getAllAccountsForUser(USER_ID)).thenReturn(accounts);
        when(portfolioService.getAllHoldingsForUser(eq(USER_ID), any(Pageable.class))).thenReturn(holdings);
        when(portfolioService.getTargetAllocationsForUser(USER_ID)).thenReturn(targets);
        when(portfolioService.getRecentAlertsForUser(USER_ID)).thenReturn(List.of(alertDTO));
        when(portfolioService.getCurrentPrices()).thenReturn(prices);
        when(portfolioService.initializeAccountTypeTotals()).thenReturn(accountTypeTotals);
        when(portfolioService.buildAccountTypeMap(accounts)).thenReturn(accountTypeMap);
        when(portfolioService.getUsdToSekRate()).thenReturn(usdToSekRate);
        when(portfolioService.enrichSingleHolding(holding, prices, usdToSekRate)).thenReturn(enrichedHoldingDTO);
        when(portfolioService.calculatePortfolioTotals(holdings, prices, accountTypeMap, accountTypeTotals))
                .thenReturn(1000.0);
        when(portfolioService.detectDrift(accountTypeTotals, targets, 1000.0))
                .thenReturn(List.of(allocationRowDTO));
        when(portfolioService.getAccountSummary(accounts, accountTypeTotals, 1000.0))
                .thenReturn(List.of(accountSummaryDTO));

        return new PortfolioResponseDTO(
                List.of(accountSummaryDTO),
                List.of(enrichedHoldingDTO),
                List.of(allocationRowDTO),
                1000.0,
                List.of(alertDTO),
                true,
                usdToSekRate);
    }
}
