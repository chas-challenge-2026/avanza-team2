package se.comerit.avanza.service;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.UserRepository;
import se.comerit.avanza.security.JwtUtil;
import se.comerit.avanza.dto.auth.LoginRequestDTO;
import se.comerit.avanza.dto.auth.LoginResponseDTO;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil = new JwtUtil(
            "4f1a9c2e6b7d3f80512e9a6c4b3d7f102a8e5c9b6d3f1a7e4c8b2d6f9a3e5c71",
            3600000);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(passwordEncoder, userRepository, jwtUtil);
    }

    @Test
    @DisplayName("Authenticate success - Legacy MD5 migration upgrades user to BCrypt")
    void authenticate_Success_MD5Migration() {
        LoginRequestDTO request = new LoginRequestDTO("legacy@example.com", "secret123");

        User md5User = new User();
        md5User.setId(1L);
        md5User.setName("Legacy User");
        md5User.setEmail("legacy@example.com");
        // Uses the exact same hashing logic directly from AuthService
        md5User.setPassword_md5(authService.md5Hash(request.password()));
        md5User.setPassword_bcrypt(null);

        when(userRepository.findByEmail("legacy@example.com")).thenReturn(Optional.of(md5User));
        when(passwordEncoder.encode("secret123")).thenReturn("new_bcrypt_hash");

        LoginResponseDTO response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("new_bcrypt_hash", md5User.getPassword_bcrypt());
        assertNull(md5User.getPassword_md5());
        verify(userRepository).save(md5User);
    }

    @Test
    @DisplayName("Authenticate success - Standard BCrypt user")
    void authenticate_Success_BCrypt() {
        User bcryptUser = new User();
        bcryptUser.setId(2L);
        bcryptUser.setName("John Doe");
        bcryptUser.setEmail("john@example.com");
        bcryptUser.setPassword_bcrypt("hashed_bcrypt_password");
        bcryptUser.setPassword_md5(null);

        LoginRequestDTO request = new LoginRequestDTO("john@example.com", "secret123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(bcryptUser));
        when(passwordEncoder.matches("secret123", "hashed_bcrypt_password")).thenReturn(true);

        LoginResponseDTO response = authService.authenticate(request);

        assertNotNull(response);
        assertNotNull(response.token());
        assertEquals("John Doe", response.name());
        assertEquals("john@example.com", response.email());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Authenticate failure - Invalid credentials")
    void authenticate_Failure_InvalidPassword() {
        User bcryptUser = new User();
        bcryptUser.setId(3L);
        bcryptUser.setName("John Doe");
        bcryptUser.setEmail("john@example.com");
        bcryptUser.setPassword_bcrypt("hashed_bcrypt_password");
        bcryptUser.setPassword_md5(null);

        LoginRequestDTO wrongRequest = new LoginRequestDTO("john@example.com", "wrongpassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(bcryptUser));
        when(passwordEncoder.matches("wrongpassword", "hashed_bcrypt_password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(wrongRequest));

        verify(userRepository, never()).save(any(User.class));
    }
}
