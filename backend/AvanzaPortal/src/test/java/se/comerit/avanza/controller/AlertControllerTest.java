package se.comerit.avanza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.service.AlertService;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @Mock
    private Authentication authentication;

    private AlertController alertController;

    private User user;
    private Alerts alert;

    @BeforeEach
    void setUp() {
        alertController = new AlertController(alertService);

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        alert = new Alerts(
                "DRIFT",
                "Test alert",
                false,
                new Timestamp(System.currentTimeMillis()),
                user
        );
        alert.setId(1L);
    }

    @Test
    void shouldReturnAlertsForAuthenticatedUser() {
        // Arrange
        when(authentication.getName())
                .thenReturn("test@test.com");

        Page<Alerts> alertsPage =
                new PageImpl<>(List.of(alert));

        when(alertService.getStoredAlertsByEmail(
                eq("test@test.com"),
                any(Pageable.class)
        )).thenReturn(alertsPage);

        when(alertService.generateLiveDriftAlertsByEmail(
                "test@test.com"
        )).thenReturn(List.of());

        when(alertService.getDriftThreshold())
                .thenReturn(7);

        // Act
        ResponseEntity<Map<String, Object>> response =
                alertController.listAlerts(
                        authentication,
                        0,
                        10
                );

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertNotNull(response.getBody().get("storedAlerts"));
        assertNotNull(response.getBody().get("liveAlerts"));
        assertEquals(
                7,
                response.getBody().get("driftThreshold")
        );

        verify(alertService).getStoredAlertsByEmail(
                eq("test@test.com"),
                any(Pageable.class)
        );

        verify(alertService).generateLiveDriftAlertsByEmail(
                "test@test.com"
        );

        verify(alertService).getDriftThreshold();
    }

    @Test
    void shouldDismissAlert() {
        // Arrange
        Long alertId = 1L;

        // Act
        ResponseEntity<Map<String, Object>> response =
                alertController.dismissAlert(alertId);

        // Assert
        verify(alertService).dismissAlert(alertId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Alert dismissed successfully",
                response.getBody().get("message")
        );
    }
}