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

import se.comerit.avanza.dto.alerts.LiveDriftAlertDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.Holdings;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.TargetRepository;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertsRepository alertsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TargetRepository targetRepository;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(
                alertsRepository,
                accountRepository,
                targetRepository
        );
    }

    @Test
    void shouldReturnDriftThresholdAsSevenPercent() {
        int result = alertService.getDriftThreshold();

        assertEquals(7, result);
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
                pageable
        )).thenReturn(expectedPage);

        // Act
        Page<Alerts> result =
                alertService.getStoredAlerts(userId, pageable);

        // Assert
        assertSame(expectedPage, result);

        verify(alertsRepository)
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    void shouldDismissAndSaveExistingAlert() {
        // Arrange
        Long alertId = 5L;

        Alerts alert = new Alerts();
        alert.setDismissed(false);

        when(alertsRepository.findById(alertId))
                .thenReturn(Optional.of(alert));

        // Act
        alertService.dismissAlert(alertId);

        // Assert
        assertTrue(alert.getDismissed());
        verify(alertsRepository).save(alert);
    }

    @Test
    void shouldThrowExceptionWhenAlertDoesNotExist() {
        // Arrange
        Long alertId = 999L;

        when(alertsRepository.findById(alertId))
                .thenReturn(Optional.empty());

        // Act och Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alertService.dismissAlert(alertId)
        );

        assertEquals("Alert not found", exception.getMessage());

        verify(alertsRepository, never())
                .save(any(Alerts.class));
    }

    @Test
    void shouldReturnNoLiveAlertsForEmptyData() {
        // Arrange
        Long userId = 1L;

        when(accountRepository.findByUserId(userId))
                .thenReturn(List.of());

        when(targetRepository.findByUserId(userId))
                .thenReturn(List.of());

        // Act
        List<LiveDriftAlertDTO> result =
                alertService.generateLiveDriftAlerts(userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCreateLiveAlertWhenAllocationDriftsTooMuch() {
        // Arrange
        Long userId = 1L;

        Holdings holding = new Holdings(
                "ERIC-B",
                "Ericsson",
                10,
                50.0,
                "SEK",
                null
        );

        Account account = new Account(
                "ISK",
                "Mitt ISK",
                "SEK",
                null,
                List.of(holding)
        );

        TargetAllocations iskTarget =
                new TargetAllocations("ISK", 50.0, null);

        TargetAllocations kfTarget =
                new TargetAllocations("KF", 50.0, null);

        when(accountRepository.findByUserId(userId))
                .thenReturn(List.of(account));

        when(targetRepository.findByUserId(userId))
                .thenReturn(List.of(iskTarget, kfTarget));

        // Act
        List<LiveDriftAlertDTO> result =
                alertService.generateLiveDriftAlerts(userId);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }
}