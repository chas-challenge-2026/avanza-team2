package se.comerit.avanza.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JwtUtilTest {
    private JwtUtil jwtUtil;
    private final String secret = "4f1a9c2e6b7d3f80512e9a6c4b3d7f102a8e5c9b6d3f1a7e4c8b2d6f9a3e5c71";
    private final long expirationTime = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret, expirationTime);
    }

    @Test
    @DisplayName("Generate token - Creates valid JWT")
    void generateToken_CreatesValidJwt() {
        String username = "john";

        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT format: header.payload.signature
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("Extract username - Returns correct username from token")
    void extractUsername_ReturnsCorrectUsername() {
        String username = "john";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Extract username - Returns different user from different token")
    void extractUsername_ReturnsDifferentUser() {
        String user1 = "john@example.com";
        String user2 = "jane@example.com";
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        assertEquals(user1, jwtUtil.extractUsername(token1));
        assertEquals(user2, jwtUtil.extractUsername(token2));
        assertNotEquals(user1, jwtUtil.extractUsername(token2));
    }

    @Test
    @DisplayName("Validate token - Valid token returns true")
    void validateToken_ValidToken_ReturnsTrue() {
        String username = "john@example.com";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token, username);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validate token - Invalid token returns false")
    void validateToken_InvalidToken_ReturnsFalse() {
        String username = "john@example.com";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token, "different@example.com");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token - Malformed token returns false")
    void validateToken_MalformedToken_ReturnsFalse() {
        String malformedToken = "invalid.token.format";

        boolean isValid = jwtUtil.validateToken(malformedToken, "john@example.com");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token - Empty token returns false")
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtUtil.validateToken("", "john@example.com");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Extract username - Throws exception on invalid token")
    void extractUsername_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> jwtUtil.extractUsername(invalidToken));
    }
}
