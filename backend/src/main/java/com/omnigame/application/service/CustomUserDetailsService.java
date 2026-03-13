package com.omnigame.application.service;

import com.omnigame.domain.model.User;
import com.omnigame.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Custom UserDetailsService that bridges Supabase JWT tokens with our internal database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Standard method for Spring Security (usually email or username).
     * We don't heavily use this in our stateless JWT flow natively unless simulating login.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        return new CustomUserDetails(user);
    }

    /**
     * Loads the Internal User Profile from the Supabase auth_uid.
     * If it doesn't exist, we dynamically provision it (JIT provisioning).
     */
    public CustomUserDetails loadUserBySupabaseId(String authUidStr, String email) {
        UUID authUid = UUID.fromString(authUidStr);
        User user = userRepository.findByAuthUid(authUid)
                .orElseGet(() -> {
                    log.info("Provisioning new internal user profile for Supabase UID: {}", authUid);
                    User newUser = User.builder()
                            .authUid(authUid)
                            .email(email)
                            .build();
                    return userRepository.save(newUser);
                });
        
        return new CustomUserDetails(user);
    }
}
