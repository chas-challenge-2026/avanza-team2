package se.comerit.avanza.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import se.comerit.avanza.dto.auth.LoginRequestDTO;
import se.comerit.avanza.dto.auth.LoginResponseDTO;
import se.comerit.avanza.service.AuthService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    @DisplayName("Login success - Returns 200 with token")
    void login_Success() {
        LoginRequestDTO request = new LoginRequestDTO("john@example.com", "password123");
        LoginResponseDTO response = new LoginResponseDTO("john_token", "John Doe", "john@example.com");

        when(authService.authenticate(request)).thenReturn(response);

        ResponseEntity<LoginResponseDTO> result = authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("john_token", result.getBody().token());
        assertEquals("John Doe", result.getBody().name());
        assertEquals("john@example.com", result.getBody().email());

        verify(authService).authenticate(request);
    }

    @Test
    @DisplayName("Login failure - Invalid credentials")
    void login_Failure_InvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO("john@example.com", "wrongpassword");

        when(authService.authenticate(request))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        assertThrows(IllegalArgumentException.class, () -> authController.login(request));
    }

    @Test
    @DisplayName("Logout - Returns 204 No Content")
    void logout_Success() {
        ResponseEntity<Void> result = authController.logout();

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
    }
}
