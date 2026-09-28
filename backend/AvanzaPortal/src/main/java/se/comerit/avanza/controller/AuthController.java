package se.comerit.avanza.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.comerit.avanza.dto.auth.LoginRequestDTO;
import se.comerit.avanza.dto.auth.LoginResponseDTO;
import se.comerit.avanza.service.AuthService;

/**
 * Controller responsible for handling authentication-related HTTP requests.
 *
 * The controller handles login, logout and authentication status requests.
 * Authentication and password verification are handled by AuthService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates an AuthController with the required AuthService.
     *
     * @param authService service responsible for authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Attempts to authenticate a user using their email and password.
     *
     * Authentication is delegated to AuthService.
     * If authentication succeeds, the JWT is stored in an HttpOnly cookie.
     *
     * @param loginRequest the login request containing email and password
     * @return a ResponseEntity containing the login response
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO loginRequest) {

        LoginResponseDTO response = authService.authenticate(loginRequest);

        // Store the JWT in an HttpOnly cookie so JavaScript cannot access it.
        ResponseCookie cookie = ResponseCookie.from("jwt", response.token())
                .httpOnly(true)
                // HTTPS is required for Secure cookies in production.
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(60 * 60)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    /**
     * Checks whether the current request is authenticated.
     *
     * The JWT is read from the HttpOnly cookie by JwtFilter before
     * this endpoint is reached.
     *
     * @param authentication the current Spring Security authentication
     * @return 204 if the user is authenticated
     */
    @GetMapping("/me")
    public ResponseEntity<Void> me(Authentication authentication) {
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs out the current user by clearing the JWT cookie.
     *
     * @return a ResponseEntity with 204 No Content status
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout() {

        // Clear the JWT cookie by setting its max age to zero.
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}