package com.omnigame.presentation.rest;

import com.omnigame.application.service.CustomUserDetails;
import com.omnigame.domain.model.User;
import com.omnigame.presentation.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Authentication and Current User operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "auth-controller", description = "Endpoints for user session state")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userDetails.getUser();
        
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getSubscriptionTier(),
                user.getRole(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
