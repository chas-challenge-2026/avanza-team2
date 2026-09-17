package se.comerit.avanza.security;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpServletRequest;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Valid JWT token - Sets authentication in context")
    void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        String token = "valid_jwt_token";
        String username = "john@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token, username)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, chain);

        // Verify authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // Verify filter chain continued
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Invalid JWT token - Does not set authentication")
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws Exception {
        String token = "invalid_token";
        String username = "john@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token, username)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, chain);

        // Verify authentication was NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Missing Authorization header - Continues without authentication")
    void doFilterInternal_NoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Malformed Authorization header - Continues without authentication")
    void doFilterInternal_MalformedHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic xyz123");

        jwtFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT extraction throws exception - Clears security context")
    void doFilterInternal_ExtractionThrowsException() throws Exception {
        String token = "broken_token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Invalid token format"));

        jwtFilter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}
