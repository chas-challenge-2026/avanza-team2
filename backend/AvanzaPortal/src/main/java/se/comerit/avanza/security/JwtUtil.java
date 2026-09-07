package se.comerit.avanza.security;

import org.springframework.stereotype.Component;

/**
 * Utility class for necessary JWT operations.
 * JwtUtil
 */
@Component
public class JwtUtil {
    // Instance of SecurityConfig to access password encoder.
    private SecurityConfig securityConfig;

    public JwtUtil(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    /**
     * Hashes a raw user password with BCrypt. Callers are responsible for
     * persisting
     *
     * @param rawPassword the plaintext password to hash
     * @return the BCrypt hash to store in place of the plaintext password
     */
    public String hashPassword(String rawPassword) {
        return securityConfig.passwordEncoder().encode(rawPassword);
    }
}