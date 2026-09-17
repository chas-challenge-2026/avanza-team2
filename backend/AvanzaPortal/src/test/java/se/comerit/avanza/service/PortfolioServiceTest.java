package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.dto.portfolio.AccountSummaryDTO;
import se.comerit.avanza.dto.portfolio.AllocationRowDTO;
import se.comerit.avanza.dto.portfolio.EnrichedHoldingDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.TargetRepository;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private HoldingsRepository holdingsRepository;

        @Mock
        private TargetRepository targetRepository;

        @Mock
        private AlertsRepository alertsRepository;

        @Mock
        private MarketService marketService;

        @InjectMocks
        private PortfolioService portfolioService;

        @BeforeEach
        void setUp() {
        }

        @Test
        void shouldInitializeAllAccountTypeTotalsToZero() {
                // Act
                Map<String, Double> result = portfolioService.initializeAccountTypeTotals();

                // Assert
                assertEquals(4, result.size());
                assertEquals(0.0, result.get("ISK"));
                assertEquals(0.0, result.get("KF"));
                assertEquals(0.0, result.get("Depa"));
                assertEquals(0.0, result.get("Pension"));
        }

        @Test
        void shouldBuildAccountTypeMap() {
                // Arrange
                Account iskAccount = new Account();
                iskAccount.setId(1L);
                iskAccount.setAccount_type("ISK");

                Account kfAccount = new Account();
                kfAccount.setId(2L);
                kfAccount.setAccount_type("KF");

                // Act
                Map<Long, String> result = portfolioService.buildAccountTypeMap(
                                List.of(iskAccount, kfAccount));

                // Assert
                assertEquals(2, result.size());
                assertEquals("ISK", result.get(1L));
                assertEquals("KF", result.get(2L));
        }

        @Test
        void shouldCalculateHoldingValueInSek() {
                // Arrange
                Holdings holding = new Holdings(
                                "ERIC-B",
                                "Ericsson",
                                10,
                                50.0,
                                "SEK",
                                null);

                Map<String, Double> prices = portfolioService.getCurrentPrices();

                // Act
                EnrichedHoldingDTO result = portfolioService.enrichSingleHolding(holding, prices);

                // Assert
                assertEquals(74.20, result.currentPrice(), 0.001);
                assertEquals(742.0, result.valueSek(), 0.001);
                assertEquals(242.0, result.unrealizedReturn(), 0.001);
                assertEquals(48.4, result.unrealizedReturnPct(), 0.001);
                assertEquals("SEK", result.displayCurrency());
        }

        @Test
        void shouldConvertUsdHoldingValueToSek() {
                // Arrange
                when(marketService.getFx("USD", "SEK"))
                                .thenReturn(new FxRateResponseDTO("2026-09-16", "USD", "SEK", 10.45));

                Holdings holding = new Holdings(
                                "AAPL",
                                "Apple",
                                2,
                                100.0,
                                "USD",
                                null);

                Map<String, Double> prices = portfolioService.getCurrentPrices();

                // Act
                EnrichedHoldingDTO result = portfolioService.enrichSingleHolding(holding, prices);

                // Assert
                assertEquals(187.32, result.currentPrice(), 0.001);
                assertEquals(3914.99, result.valueSek(), 0.001);
                assertEquals("USD→SEK", result.displayCurrency());
        }

        @Test
        void shouldCalculateTotalPortfolioValue() {
                // Arrange
                Account account = new Account();
                account.setId(10L);
                account.setAccount_type("ISK");

                Holdings holding = new Holdings(
                                "ERIC-B",
                                "Ericsson",
                                10,
                                50.0,
                                "SEK",
                                account);

                Map<Long, String> accountTypeMap = Map.of(10L, "ISK");

                Map<String, Double> accountTypeTotals = portfolioService.initializeAccountTypeTotals();

                Map<String, Double> prices = portfolioService.getCurrentPrices();

                // Act
                double result = portfolioService.calculatePortfolioTotals(
                                List.of(holding),
                                prices,
                                accountTypeMap,
                                accountTypeTotals);

                // Assert
                assertEquals(742.0, result, 0.001);
                assertEquals(742.0, accountTypeTotals.get("ISK"), 0.001);
        }

        @Test
        void shouldReturnZeroForEmptyPortfolio() {
                // Arrange
                Map<String, Double> accountTypeTotals = portfolioService.initializeAccountTypeTotals();

                // Act
                double result = portfolioService.calculatePortfolioTotals(
                                List.of(),
                                portfolioService.getCurrentPrices(),
                                Map.of(),
                                accountTypeTotals);

                // Assert
                assertEquals(0.0, result, 0.001);
                assertEquals(0.0, accountTypeTotals.get("ISK"), 0.001);
        }

        @Test
        void shouldDetectAllocationDriftOverThreshold() {
                // Arrange
                Map<String, Double> totals = new HashMap<>();
                totals.put("ISK", 600.0);
                totals.put("KF", 400.0);
                totals.put("Depa", 0.0);
                totals.put("Pension", 0.0);

                List<TargetAllocations> targets = List.of(
                                new TargetAllocations("ISK", 50.0, null),
                                new TargetAllocations("KF", 50.0, null));

                // Act
                List<AllocationRowDTO> result = portfolioService.detectDrift(totals, targets, 1000.0);

                AllocationRowDTO iskRow = result.stream()
                                .filter(row -> "ISK".equals(row.accountType()))
                                .findFirst()
                                .orElseThrow();

                // Assert
                assertEquals(60.0, iskRow.actual(), 0.001);
                assertEquals(50.0, iskRow.target(), 0.001);
                assertEquals(10.0, iskRow.drift(), 0.001);
                assertTrue(iskRow.overThreshold());
        }

        @Test
        void shouldHandleDriftWhenPortfolioIsEmpty() {
                // Arrange
                Map<String, Double> totals = portfolioService.initializeAccountTypeTotals();

                // Act
                List<AllocationRowDTO> result = portfolioService.detectDrift(
                                totals,
                                List.of(),
                                0.0);

                // Assert
                assertEquals(4, result.size());

                for (AllocationRowDTO row : result) {
                        assertEquals(0.0, row.actual(), 0.001);
                        assertFalse(row.overThreshold());
                }
        }

        @Test
        void shouldCreateAccountSummaryWithRoundedTotal() {
                // Arrange
                Account account = new Account();
                account.setId(1L);
                account.setAccount_type("ISK");
                account.setAccount_name("Mitt ISK");

                Map<String, Double> totals = Map.of("ISK", 1234.567);

                // Act
                List<AccountSummaryDTO> result = portfolioService.getAccountSummary(
                                List.of(account),
                                totals,
                                1234.567);

                // Assert
                assertEquals(1, result.size());
                assertEquals(
                                1234.57,
                                result.get(0).totalValueSek(),
                                0.001);
        }
}