package com.omnigame.presentation.dto;

import com.omnigame.domain.model.Role;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for User Profile responses.
 */
public record UserResponse(
    UUID id,
    String email,
    String displayName,
    String avatarUrl,
    String subscriptionTier,
    Role role,
    OffsetDateTime createdAt
) {}
