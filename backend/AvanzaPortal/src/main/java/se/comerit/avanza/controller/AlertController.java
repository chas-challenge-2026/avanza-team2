package se.comerit.avanza.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.comerit.avanza.dto.alerts.AlertsResponseDTO;
import se.comerit.avanza.dto.alerts.LiveDriftAlertDTO;
import se.comerit.avanza.entity.Alerts;
import se.comerit.avanza.service.AlertService;

/**
 * Controller for managing alerts related to account holdings and target
 * allocations.
 * Provides endpoints to list and dismiss alerts for the currently logged-in
 * user.
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Get a paginated list of alerts for the currently logged-in user.
     *
     * The authenticated user's email is retrieved from Spring Security instead
     * of using an HTTP session. The email is then used by the service layer to
     * identify the corresponding user.
     *
     * @param authentication the authentication information provided by Spring
     *                       Security
     * @param page           the page number for pagination
     * @param size           the number of alerts per page
     * @return a response entity containing the paginated list of alerts and live
     *         drift alerts
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listAlerts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Get the authenticated user's email from the JWT authentication
        String email = authentication.getName();

        Pageable pageable = PageRequest.of(page, size);

        // Get stored alerts and convert to DTO
        Page<Alerts> storedAlertsPage = alertService.getStoredAlertsByEmail(email, pageable);
        Page<AlertsResponseDTO> storedAlerts = storedAlertsPage.map(alert -> new AlertsResponseDTO(
                alert.getId(),
                alert.getUser().getId(),
                alert.getMessage(),
                alert.getDismissed(),
                alert.getCreatedAt().toString()));

        // Get live drift alerts (already as DTOs)
        List<LiveDriftAlertDTO> liveAlerts = alertService.generateLiveDriftAlertsByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("storedAlerts", storedAlerts);
        response.put("liveAlerts", liveAlerts);
        response.put("driftThreshold", alertService.getDriftThreshold());

        return ResponseEntity.ok(response);
    }

    /**
     * Dismiss an alert for the currently logged-in user.
     *
     * Authentication is handled by Spring Security, so no HTTP session is
     * required. The alert itself is dismissed by the service layer.
     *
     * @param id             the ID of the alert to be dismissed
     * @param authentication the authentication information provided by Spring
     *                       Security
     * @return a response entity indicating the result of the dismissal operation
     */
    @PutMapping("/{id}/dismiss")
    public ResponseEntity<Map<String, Object>> dismissAlert(
            @PathVariable Long id, Authentication authentication) {

        alertService.dismissAlert(id, authentication.getName());

        return ResponseEntity.ok(Map.of("message", "Alert dismissed successfully"));
    }
}