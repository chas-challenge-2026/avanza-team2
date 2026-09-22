package se.comerit.avanza.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Provides a BCrypt password encoder bean with a strength of 12.
     *
     * @return a BCryptPasswordEncoder instance with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS for requests from the React frontend.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF for stateless API.
                .csrf(AbstractHttpConfigurer::disable)

                // Use stateless session policy.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define endpoint access rules.
                .authorizeHttpRequests(auth -> auth
                        // Login and logout are public endpoints.
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/logout"
                        ).permitAll()

                        // All other endpoints require authentication.
                        .anyRequest().authenticated())

                // Run JwtFilter before Spring's default authentication filter.
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS for the React frontend running locally on port 5173.
     *
     * @return the CORS configuration source used by Spring Security
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from the local React/Vite frontend.
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // Allow the HTTP methods used by the frontend.
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        // Only allow the request headers used by the application.
        configuration.setAllowedHeaders(
                List.of("Content-Type", "Authorization")
        );

        // Allow the browser to send HttpOnly cookies with requests.
        configuration.setAllowCredentials(true);

        // Apply this CORS configuration to all endpoints.
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}