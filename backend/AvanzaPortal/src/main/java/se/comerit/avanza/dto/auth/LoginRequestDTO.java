package se.comerit.avanza.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record LoginRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String password) {

}
