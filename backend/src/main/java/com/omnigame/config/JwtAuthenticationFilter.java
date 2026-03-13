package com.omnigame.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import com.omnigame.application.service.CustomUserDetailsService;
import com.omnigame.application.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JWT authentication filter integrated with Supabase Auth.
 *
 * <p>Extracts the Bearer token from the Authorization header, validates it
 * using the Supabase JWT secret, and populates the Spring Security context
 * with the authenticated user's identity and claims.</p>
 *
 * <p><strong>Token Flow:</strong></p>
 * <ol>
 *   <li>Frontend authenticates via Supabase Auth UI → receives JWT</li>
 *   <li>Frontend includes JWT in {@code Authorization: Bearer <token>} header</li>
 *   <li>This filter validates the JWT signature and expiration</li>
 *   <li>Extracts {@code sub} (user ID) and {@code role} claims</li>
 *   <li>Sets {@link SecurityContextHolder} with authenticated principal</li>
 * </ol>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject();
                String email = claims.get("email", String.class);

                if (userId != null) {
                    CustomUserDetails userDetails = customUserDetailsService.loadUserBySupabaseId(userId, email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user: authUid={}, role={}", userId, userDetails.getUser().getRole());
                }
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            // Don't throw — let the security chain handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the Bearer token from the Authorization header.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
