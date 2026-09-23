package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import se.comerit.avanza.dto.alerts.AlertsResponseDTO;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.dto.portfolio.AccountSummaryDTO;
import se.comerit.avanza.dto.portfolio.AllocationRowDTO;
import se.comerit.avanza.dto.portfolio.EnrichedHoldingDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.UserRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.TargetRepository;
import se.comerit.avanza.nativebridge.RiskLibrary;
import se.comerit.avanza.entity.User;

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

        @Mock
        private RiskLibrary riskLibrary;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private PortfolioService portfolioService;

        @Test
        void shouldFindByEmailAndReturnUser() {
                // Arrange
                String email = "test@example.com";
                User user = new User();
                user.setEmail(email);
                when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

                // Act
                Optional<User> result = portfolioService.findByEmail(email);

                // Assert
                assertTrue(result.isPresent());
                assertEquals(email, result.get().getEmail());
        }

        @Test
        void shouldGetAllAccountsForUser() {
                // Arrange
                Long userId = 1L;
                Account account1 = new Account();
                account1.setId(1L);
                Account account2 = new Account();
                account2.setId(2L);
                when(accountRepository.findAllByUser_Id(userId)).thenReturn(List.of(account1, account2));

                // Act
                List<Account> result = portfolioService.getAllAccountsForUser(userId);

                // Assert
                assertEquals(2, result.size());
                assertEquals(1L, result.get(0).getId());
                assertEquals(2L, result.get(1).getId());
        }

        @Test
        void shouldGetAllHoldingsForUser() {
                // Arrange
                Long userId = 1L;
                PageRequest pageable = PageRequest.of(0, 10);
                Holdings holding1 = new Holdings("ERIC-B", "Ericsson", new BigDecimal("10"),
                                new BigDecimal("50.0"), "SEK", null);
                Holdings holding2 = new Holdings("VOLV-B", "Volvo", new BigDecimal("5"),
                                new BigDecimal("100.0"), "SEK", null);
                when(holdingsRepository.findAllByUserId(userId, pageable))
                                .thenReturn(new PageImpl<>(List.of(holding1, holding2)));

                // Act
                List<Holdings> result = portfolioService.getAllHoldingsForUser(userId, pageable);

                // Assert
                assertEquals(2, result.size());
                assertEquals("ERIC-B", result.get(0).getTicker());
                assertEquals("VOLV-B", result.get(1).getTicker());
        }

        @Test
        void shouldGetTargetAllocationsForUser() {
                // Arrange
                Long userId = 1L;
                List<TargetAllocations> targetAllocations = List.of(
                                new TargetAllocations("ISK", 0.4, null),
                                new TargetAllocations("KF", 0.3, null),
                                new TargetAllocations("Depa", 0.2, null),
                                new TargetAllocations("Pension", 0.1, null));
                when(targetRepository.findByUser_Id(userId)).thenReturn(targetAllocations);

                // Act
                List<TargetAllocations> result = portfolioService.getTargetAllocationsForUser(userId);

                // Assert
                assertEquals(4, result.size());
                assertEquals("ISK", result.get(0).getAccount_type());
                assertEquals("KF", result.get(1).getAccount_type());
                assertEquals("Depa", result.get(2).getAccount_type());
                assertEquals("Pension", result.get(3).getAccount_type());
        }

        @Test
        void shouldGetRecentAlertsForUser() {
                // Arrange
                Long userId = 1L;

                User user = new User();
                user.setId(userId);

                Alerts olderAlert = new Alerts();
                olderAlert.setId(11L);
                olderAlert.setUser(user);
                olderAlert.setMessage("Older alert");
                olderAlert.setDismissed(false);
                olderAlert.setCreatedAt(java.sql.Timestamp.valueOf("2026-09-15 09:00:00"));

                Alerts newerAlert = new Alerts();
                newerAlert.setId(12L);
                newerAlert.setUser(user);
                newerAlert.setMessage("Latest drift alert");
                newerAlert.setDismissed(false);
                newerAlert.setCreatedAt(java.sql.Timestamp.valueOf("2026-09-16 09:00:00"));

                when(alertsRepository.findByUser_IdAndDismissedFalseOrderByCreatedAtDesc(userId))
                                .thenReturn(List.of(newerAlert, olderAlert));

                // Act
                List<AlertsResponseDTO> result = portfolioService.getRecentAlertsForUser(userId);

                // Assert
                assertEquals(2, result.size());
                assertEquals(12L, result.get(0).id());
                assertEquals(userId, result.get(0).user());
                assertEquals("Latest drift alert", result.get(0).message());
                assertFalse(result.get(0).dismissed());
                assertEquals("2026-09-16 09:00:00.0", result.get(0).createdAt());
                assertEquals(11L, result.get(1).id());
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
                                new BigDecimal("10"),
                                new BigDecimal("50.00"),
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
                                new BigDecimal("2"),
                                new BigDecimal("100.00"),
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
                                new BigDecimal("10"),
                                new BigDecimal("50.00"),
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
        void shouldSkipHoldingWhenAccountTypeIsNull() {
                // Arrange
                Account account = new Account();
                account.setId(10L);
                account.setAccount_type(null);

                Holdings holding = new Holdings(
                                "ERIC-B",
                                "Ericsson",
                                new BigDecimal("10"),
                                new BigDecimal("50.00"),
                                "SEK",
                                account);

                Map<Long, String> accountTypeMap = Map.of(); // No account types

                Map<String, Double> accountTypeTotals = portfolioService.initializeAccountTypeTotals();

                Map<String, Double> prices = portfolioService.getCurrentPrices();

                // Act
                double result = portfolioService.calculatePortfolioTotals(
                                List.of(holding),
                                prices,
                                accountTypeMap,
                                accountTypeTotals);

                // Assert
                assertEquals(0.0, result, 742.0);
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
        void shouldCalculatePortfolioSharpeFromHistoricalValues() {
                // Arrange
                when(riskLibrary.risk_calc_sharpe_ratio_double(any(double[].class), anyLong(), eq(0.02), eq(252L)))
                                .thenReturn(1.75);

                // Act
                double result = portfolioService.calculatePortfolioSharpeRatio(
                                List.of(100.0, 110.0, 104.5, 113.0),
                                0.02);

                // Assert
                assertEquals(1.75, result, 0.001);
        }

        @Test
        void shouldReturnZeroSharpeRatio() {
                // Act
                double result = portfolioService.calculateSharpeRatio(null, 0, 0);

                // Assert
                assertEquals(0.0, result, 0.001);
        }

        @Test
        void shouldReturnZeroSharpeRatioForEmptyHistoricalValues() {
                // Act
                double result = portfolioService.calculatePortfolioSharpeRatio(
                                List.of(),
                                0.02);

                // Assert
                assertEquals(0.0, result, 0.001);
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
