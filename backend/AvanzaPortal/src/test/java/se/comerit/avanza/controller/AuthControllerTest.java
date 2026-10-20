package se.comerit.avanza.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

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

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    @DisplayName("Login success - Returns 200 with token")
    void login_Success() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("john@example.com", "password123");
        LoginResponseDTO response = new LoginResponseDTO("john_token", "John Doe", "john@example.com");

        when(authService.authenticate(loginRequest)).thenReturn(response);

        ResponseEntity<LoginResponseDTO> result = authController.login(loginRequest, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("john_token", result.getBody().token());
        assertEquals("John Doe", result.getBody().name());
        assertEquals("john@example.com", result.getBody().email());

        String cookie = result.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertTrue(cookie.contains("jwt=john_token"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
        assertFalse(cookie.contains("Secure"));

        verify(authService).authenticate(loginRequest);
    }

    @Test
    @DisplayName("Login failure - Invalid credentials")
    void login_Failure_InvalidCredentials() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("john@example.com", "wrongpassword");

        when(authService.authenticate(loginRequest))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        assertThrows(IllegalArgumentException.class, () -> authController.login(loginRequest, request));
    }

    @Test
    @DisplayName("Login over HTTPS - Sets a secure JWT cookie")
    void login_OverHttps_SetsSecureCookie() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("john@example.com", "password123");
        LoginResponseDTO response = new LoginResponseDTO("john_token", "John Doe", "john@example.com");

        when(request.isSecure()).thenReturn(true);
        when(authService.authenticate(loginRequest)).thenReturn(response);

        ResponseEntity<LoginResponseDTO> result = authController.login(loginRequest, request);

        String cookie = result.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
    }

    @Test
    @DisplayName("Me endpoint - Returns 204 No Content")
    void meEndpoint_ReturnsNoContent() {
        ResponseEntity<Void> result = authController.me(authentication);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    @DisplayName("Logout - Returns 204 No Content")
    void logout_Success() {
        ResponseEntity<Void> result = authController.logout(request);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());

        String cookie = result.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertTrue(cookie.contains("jwt="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("HttpOnly"));
    }
}
