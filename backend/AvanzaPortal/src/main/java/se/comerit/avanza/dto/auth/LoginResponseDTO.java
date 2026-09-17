package se.comerit.avanza.dto.auth;

public record LoginResponseDTO(
        String token,
        String name,
        String email) {
}
