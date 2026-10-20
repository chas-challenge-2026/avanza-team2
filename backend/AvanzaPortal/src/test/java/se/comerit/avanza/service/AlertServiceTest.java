package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import se.comerit.avanza.dto.alerts.LiveDriftAlertDTO;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.TargetRepository;
import se.comerit.avanza.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

        @Mock
        private AlertsRepository alertsRepository;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private TargetRepository targetRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private MarketService marketService;

        @Mock
        private DriftTresholdConfig tresholdConfig;

        private AlertService alertService;

        @BeforeEach
        void setUp() {
                alertService = new AlertService(
                                alertsRepository,
                                accountRepository,
                                targetRepository,
                                userRepository,
                                tresholdConfig,
                                marketService);
        }

        @Test
        void shouldReturnDriftThresholdAsSevenPercent() {
                when(tresholdConfig.getDriftThreshold()).thenReturn(0.05);

                double result = alertService.getDriftThreshold();

                assertEquals(0.05, result);
        }

        @Test
        void shouldReturnStoredAlertsForUser() {
                // Arrange
                Long userId = 1L;
                Pageable pageable = PageRequest.of(0, 10);

                Alerts alert = new Alerts();
                Page<Alerts> expectedPage = new PageImpl<>(List.of(alert));

                when(alertsRepository.findByUserIdOrderByCreatedAtDesc(
                                userId,
                                pageable)).thenReturn(expectedPage);

                // Act
                Page<Alerts> result = alertService.getStoredAlerts(userId, pageable);

                // Assert
                assertSame(expectedPage, result);

                verify(alertsRepository)
                                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        @Test
        void shouldReturnStoredAlertsByEmail() {
                // Arrange
                String email = "test@test.com";
                Long userId = 42L;
                Pageable pageable = PageRequest.of(0, 10);

                Alerts alert = new Alerts();
                Page<Alerts> expectedPage = new PageImpl<>(List.of(alert));
                User user = new User();
                user.setId(userId);

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
                when(alertsRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                                .thenReturn(expectedPage);

                // Act
                Page<Alerts> result = alertService.getStoredAlertsByEmail(email, pageable);

                // Assert
                assertSame(expectedPage, result);
                verify(userRepository).findByEmail(email);
                verify(alertsRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        @Test
        void shouldDismissAndSaveExistingAlert() {
                // Arrange
                Long alertId = 5L;
                String email = "test@test.com";

                User user = new User();
                user.setEmail(email);

                Alerts alert = new Alerts();
                alert.setDismissed(false);
                alert.setUser(user);

                when(alertsRepository.findById(alertId))
                                .thenReturn(Optional.of(alert));

                // Act
                alertService.dismissAlert(alertId, email);

                // Assert
                assertTrue(alert.getDismissed());
                verify(alertsRepository).save(alert);
        }

        @Test
        void shouldThrowExceptionWhenAlertDoesNotExist() {
                // Arrange
                Long alertId = 999L;
                String email = "test@test.com";

                when(alertsRepository.findById(alertId))
                                .thenReturn(Optional.empty());

                // Act och Assert
                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> alertService.dismissAlert(alertId, email));

                assertEquals("Alert not found", exception.getMessage());

                verify(alertsRepository, never())
                                .save(any(Alerts.class));
        }

        @Test
        void shouldNotDismissAlertAndThrowExceptionWhenUserNotAuthorized() {
                // Arrange
                Long alertId = 5L;
                String email = "unauthorized@test.com";

                User user = new User();
                user.setEmail("authorized@test.com");

                Alerts alert = new Alerts();
                alert.setDismissed(false);
                alert.setUser(user);

                when(alertsRepository.findById(alertId))
                                .thenReturn(Optional.of(alert));

                // Act and Assert
                AccessDeniedException exception = assertThrows(
                                AccessDeniedException.class,
                                () -> alertService.dismissAlert(alertId, email));

                assertEquals("User not authorized to dismiss this alert", exception.getMessage());

                verify(alertsRepository, never())
                                .save(any(Alerts.class));
        }

        @Test
        void shouldReturnNoLiveAlertsForEmptyData() {
                // Arrange
                Long userId = 1L;

                when(accountRepository.findAllWithHoldingsByUserId(userId))
                                .thenReturn(List.of());

                when(targetRepository.findByUserId(userId))
                                .thenReturn(List.of());

                // Act
                List<LiveDriftAlertDTO> result = alertService.generateLiveDriftAlerts(userId);

                // Assert
                assertTrue(result.isEmpty());
                verify(accountRepository).findAllWithHoldingsByUserId(userId);
        }

        @Test
        void shouldCreateLiveAlertWhenAllocationDriftsTooMuch() {
                // Arrange
                Long userId = 1L;

                Holdings holding = new Holdings(
                                "ERIC-B",
                                "Ericsson",
                                new BigDecimal("10"),
                                new BigDecimal("50.00"),
                                "SEK",
                                null);

                Account account = new Account(
                                "ISK",
                                "Mitt ISK",
                                "SEK",
                                null,
                                List.of(holding));

                TargetAllocations iskTarget = new TargetAllocations("ISK", 50.0, null);

                TargetAllocations kfTarget = new TargetAllocations("KF", 50.0, null);

                when(accountRepository.findAllWithHoldingsByUserId(userId))
                                .thenReturn(List.of(account));

                when(targetRepository.findByUserId(userId))
                                .thenReturn(List.of(iskTarget, kfTarget));

                when(marketService.getPrice("ERIC-B"))
                                .thenReturn(Optional.of(new BigDecimal("74.20")));

                // Act
                List<LiveDriftAlertDTO> result = alertService.generateLiveDriftAlerts(userId);

                // Assert
                assertFalse(result.isEmpty());
                assertEquals(2, result.size());
        }

        @Test
        void shouldSkipHoldingWhenMarketPriceIsUnavailable() {
                Long userId = 1L;

                Holdings holding = new Holdings(
                                "UNKNOWN",
                                "Unknown stock",
                                new BigDecimal("10"),
                                new BigDecimal("50.00"),
                                "SEK",
                                null);

                Account account = new Account(
                                "ISK",
                                "Mitt ISK",
                                "SEK",
                                null,
                                List.of(holding));

                when(accountRepository.findAllWithHoldingsByUserId(userId))
                                .thenReturn(List.of(account));

                when(targetRepository.findByUserId(userId))
                                .thenReturn(List.of());

                when(marketService.getPrice("UNKNOWN"))
                                .thenReturn(Optional.empty());

                List<LiveDriftAlertDTO> result = alertService.generateLiveDriftAlerts(userId);

                assertTrue(result.isEmpty());
        }

        @Test
        void shouldGenerateLiveDriftAlertsByEmail() {
                User user = new User();
                String email = "test@example.com";
                Long userId = 56L;
                user.setId(userId);
                user.setEmail(email);
                when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

                // Arrange

                Holdings holding = new Holdings(
                                "ERIC-B",
                                "Ericsson",
                                new BigDecimal("10"),
                                new BigDecimal("50.00"),
                                "SEK",
                                null);

                Account account = new Account(
                                "ISK",
                                "Mitt ISK",
                                "SEK",
                                null,
                                List.of(holding));

                TargetAllocations iskTarget = new TargetAllocations("ISK", 50.0, null);

                TargetAllocations kfTarget = new TargetAllocations("KF", 50.0, null);

                when(accountRepository.findAllWithHoldingsByUserId(userId))
                                .thenReturn(List.of(account));

                when(targetRepository.findByUserId(userId))
                                .thenReturn(List.of(iskTarget, kfTarget));

                when(marketService.getPrice("ERIC-B"))
                                .thenReturn(Optional.of(new BigDecimal("74.20")));

                // Act
                List<LiveDriftAlertDTO> result = alertService.generateLiveDriftAlertsByEmail(email);

                // Assert
                assertFalse(result.isEmpty());
                assertEquals(2, result.size());
        }

        @Test
        void shouldReturnGetFxRate() {
                when(marketService.getFx("USD", "SEK"))
                                .thenReturn(new FxRateResponseDTO("2026-10-14", "USD", "SEK", 10.50));

                double result = alertService.getUsdToSekRate();

                assertEquals(10.50, result);
        }
}
