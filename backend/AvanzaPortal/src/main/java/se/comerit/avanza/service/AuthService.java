package se.comerit.avanza.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import se.comerit.avanza.dto.auth.LoginRequestDTO;
import se.comerit.avanza.dto.auth.LoginResponseDTO;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.UserRepository;
import se.comerit.avanza.security.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service responsible for authentication-related business logic.
 *
 * This class handles finding users and verifying their passwords.
 * The controller should only be responsible for handling HTTP requests
 * and delegating authentication logic to this service.
 */
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Creates an AuthService with the required UserRepository.
     *
     * @param userRepository repository used to access users
     */
    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user using their email and password.
     *
     * @param email    the user's email address
     * @param password the plain-text password provided during login
     * @return the authenticated user, or null if authentication fails
     */
    @Transactional
    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Handle Legacy MD5 Password Migration
        if (user.getPassword_md5() != null) {
            if (!md5Hash(loginRequest.password()).equals(user.getPassword_md5())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            // Seamlessly upgrade to BCrypt
            user.setPassword_bcrypt(passwordEncoder.encode(loginRequest.password()));
            user.setPassword_md5(null);
            userRepository.save(user);
        } else {
            // Standard BCrypt path
            if (!passwordEncoder.matches(loginRequest.password(), user.getPassword_bcrypt())) {
                throw new BadCredentialsException("Invalid credentials");
            }
        }

        return new LoginResponseDTO(jwtUtil.generateToken(user.getEmail()), user.getName(), user.getEmail());
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
    public String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "MD5 algorithm is not available",
                    e);
        }
    }
}