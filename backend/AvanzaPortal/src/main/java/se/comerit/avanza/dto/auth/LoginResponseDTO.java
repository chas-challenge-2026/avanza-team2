package se.comerit.avanza.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginResponseDTO(
        @JsonIgnore String token,
        String name,
        String email) {
}