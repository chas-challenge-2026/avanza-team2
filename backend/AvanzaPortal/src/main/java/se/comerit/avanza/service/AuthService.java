package se.comerit.avanza.service;

import org.springframework.stereotype.Service;

import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Service responsible for authentication-related business logic.
 *
 * This class handles finding users and verifying their passwords.
 * The controller should only be responsible for handling HTTP requests
 * and delegating authentication logic to this service.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    /**
     * Creates an AuthService with the required UserRepository.
     *
     * @param userRepository repository used to access users
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user using their email and password.
     *
     * @param email the user's email address
     * @param password the plain-text password provided during login
     * @return the authenticated user, or null if authentication fails
     */
    public User authenticate(String email, String password) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        String passwordHash = md5Hash(password);

        if (!passwordHash.equals(user.getPassword_md5())) {
            return null;
        }

        return user;
    }

    /**
     * Creates an MD5 hash from the provided password.
     *
     * Note: MD5 is currently used for compatibility with the existing
     * authentication system. This will be migrated to BCrypt as part
     * of the security improvements planned for v2.
     *
     * @param input the password to hash
     * @return the MD5 hash as a hexadecimal string
     */
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] hashBytes = md.digest(
                    input.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "MD5 algorithm is not available",
                    e
            );
        }
    }
}