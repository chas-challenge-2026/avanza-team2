package se.comerit.avanza.dto.alertDto;

public record AlertResponseDTO(
        Long id,
        Long userId,
        String message,
        boolean dismissed,
        String createdAt) {
}
