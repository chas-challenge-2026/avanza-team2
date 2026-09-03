package se.comerit.avanza.dto.alerts;

public record LiveDriftAlertDTO(
        String alertType,
        String message,
        boolean dismissed,
        String createdAt) {
}
