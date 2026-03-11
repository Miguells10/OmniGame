package com.omnigame.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for OmniGame AI.
 *
 * <p>Implements a stateless JWT-based security model integrated with
 * Supabase Auth. All API requests pass through the {@link JwtAuthenticationFilter}
 * for token validation.</p>
 *
 * <h3>Access Rules:</h3>
 * <ul>
 *   <li><strong>Public:</strong> GET /api/v1/games/**, actuator health, OPTIONS</li>
 *   <li><strong>Authenticated:</strong> POST /api/v1/games, /api/v1/collector/**</li>
 * </ul>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless API — no CSRF, no sessions
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — game catalog browsing
                        .requestMatchers(HttpMethod.GET, "/api/v1/games/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Actuator health check
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Protected endpoints — require JWT
                        .requestMatchers("/api/v1/collector/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/games/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/games/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/games/**").authenticated()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // JWT filter — runs before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
