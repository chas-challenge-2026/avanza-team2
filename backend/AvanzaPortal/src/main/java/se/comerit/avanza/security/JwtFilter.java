package se.comerit.avanza.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Custom Spring Security filter that intercepts incoming HTTP
 * requests one by one,
 * each request is extracted and validated for JWT tokens from the
 * 'Authorization' header.
 * <p>
 * If a valid token is present, the user's authentication details are set in the
 * security context. Authorization rules (access control)
 * are handled downstream by Spring Security's filter chain.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    // Instancieting JWT utility to get all methods related to JWT operations.
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        // Extract the JWT token from the Authorization header and validate it
        String header = request.getHeader("Authorization");

        // Check if the Authorization header contains a Bearer token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // Extract the username from the token and validate it
            try {
                String username = jwtUtil.extractUsername(token);

                // Check if user is not already authenticated in the context
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Validate the token before setting authentication in the context
                    if (jwtUtil.validateToken(token, username)) {

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        // Continue the filter chain regardless of JWT validation outcome
        chain.doFilter(request, response);
    }
}
