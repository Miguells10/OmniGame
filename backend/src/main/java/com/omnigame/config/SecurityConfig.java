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

import com.omnigame.application.service.SecurityAuditService;
import com.omnigame.application.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    private final SecurityAuditService securityAuditService;

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
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/games/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Actuator health check
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Auth details endpoint
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()

                        // Collector Chat (Require Authenticated Tiers)
                        .requestMatchers(HttpMethod.POST, "/api/v1/collector/chat").hasAnyRole("USER", "MODDER", "ADMIN")

                        // Game Writing Tiers
                        .requestMatchers(HttpMethod.POST, "/api/v1/games/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/games/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/games/**").hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Exception Handling for Audit Logs
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            handleAccessDeniedLog(request);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            handleUnauthorizedLog(request);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
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

    private void handleAccessDeniedLog(HttpServletRequest request) {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            securityAuditService.logEvent(
                    userDetails.getUser().getId(),
                    userDetails.getUser().getAuthUid().toString(),
                    "ACCESS_DENIED",
                    request.getRequestURI(),
                    "FORBIDDEN",
                    request.getRemoteAddr(),
                    "User lacks required role."
            );
        }
    }

    private void handleUnauthorizedLog(HttpServletRequest request) {
        securityAuditService.logEvent(
                null, 
                "Anonymous", 
                "UNAUTHORIZED_ACCESS", 
                request.getRequestURI(), 
                "UNAUTHORIZED", 
                request.getRemoteAddr(), 
                "No valid JWT token provided."
        );
    }
}
