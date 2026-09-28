package se.comerit.avanza.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for necessary JWT operations.
 * JwtUtil
 */
@Component
public class JwtUtil {
    // Instance variables for JWT expiration time and signing key from Properties.
    private final long expirationTime;
    private final SecretKey signingKey;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationTime) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    /**
     * Generates a JWT token for the specified user.
     * It is using the configured signing key,
     * and estimated expirationtime.
     *
     * @param user the username for which to generate the token
     * @return the generated JWT token in string format.
     */
    public String generateToken(String user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token the JWT token from which to extract the username
     * @return the username (subject) contained in the token
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates a JWT token against the specified user.
     *
     * @param token the JWT token to validate
     * @param user  the username to compare against the token's subject
     * @return true if the token is valid, unexpired, and matches the user, false
     *         otherwise
     */
    public boolean validateToken(String token, String user) {
        try {
            return extractUsername(token).equals(user);
        } catch (Exception e) {
            return false;
        }
    }
}