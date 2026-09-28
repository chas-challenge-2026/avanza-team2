package se.comerit.avanza.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Custom Spring Security filter that intercepts incoming HTTP
 * requests one by one.
 *
 * The filter extracts and validates the JWT token from the HttpOnly
 * cookie named "jwt".
 *
 * If a valid token is present, the user's authentication details
 * are set in the security context.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    // Instantiating JWT utility to get all methods related to JWT operations.
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

        String token = null;

        // Extract the JWT token from the HttpOnly cookie.
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Validate the JWT if a token was found.
        if (token != null && !token.isBlank()) {
            try {
                // Extract the username from the token and validate it.
                String username = jwtUtil.extractUsername(token);

                // Check if user is not already authenticated in the context.
                if (username != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Validate the token before setting authentication in the context.
                    if (jwtUtil.validateToken(token, username)) {

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        List.of());

                        auth.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request));

                        SecurityContextHolder.getContext()
                                .setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // Invalid or expired JWT - leave the request unauthenticated.
                SecurityContextHolder.clearContext();
            }
        }

        // Continue the filter chain regardless of JWT validation outcome.
        chain.doFilter(request, response);
    }
}