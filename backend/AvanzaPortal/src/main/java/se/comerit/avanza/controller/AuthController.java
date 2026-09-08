package se.comerit.avanza.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import se.comerit.avanza.dto.auth.LoginRequestDTO;
import se.comerit.avanza.dto.auth.LoginResponseDTO;
import se.comerit.avanza.service.AuthService;

/**
 * Controller responsible for handling authentication-related HTTP requests.
 *
 * The controller handles login and logout requests and manages the user
 * session.
 * Authentication and password verification are handled by AuthService.
 */
@RestController
@RequestMapping("/api")
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
     * If authentication succeeds, a LoginResponseDTO is returned.
     *
     * @param loginRequest the login request containing email and password
     * @return a ResponseEntity containing the login response
     */
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Modernizated version of logout for stateless JWT API.
     * Returns 204 No Content so the client can clear the JWT token from storage.
     * 
     * @return a ResponseEntity with 204 No Content status
     */
    @DeleteMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}