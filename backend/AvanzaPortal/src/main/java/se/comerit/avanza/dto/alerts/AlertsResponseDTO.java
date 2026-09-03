package se.comerit.avanza.dto.alerts;

public record AlertsResponseDTO(
                Long id,
                Long user,
                String message,
                boolean dismissed,
                String createdAt) {
}
