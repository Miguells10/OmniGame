package com.omnigame.presentation.dto;

import com.omnigame.domain.model.Game;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable response DTO for game catalog entries.
 *
 * <p>Uses Java 21 record for zero-boilerplate immutable data transport.
 * Entity counts are eagerly resolved to avoid N+1 queries in list responses.</p>
 *
 * @param id          unique game identifier
 * @param name        display name of the game
 * @param slug        URL-friendly identifier
 * @param coverUrl    cover art image URL
 * @param description game synopsis
 * @param entityCount total number of mods/assets/patches/tools
 * @param createdAt   timestamp of catalog entry creation
 */
public record GameResponse(
        UUID id,
        String name,
        String slug,
        String coverUrl,
        String description,
        long entityCount,
        Instant createdAt
) {

    /**
     * Factory method to construct a response from a domain entity.
     *
     * @param game        the game domain aggregate
     * @param entityCount pre-computed entity count
     * @return immutable response DTO
     */
    public static GameResponse from(Game game, long entityCount) {
        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getSlug(),
                game.getCoverUrl(),
                game.getDescription(),
                entityCount,
                game.getCreatedAt()
        );
    }
}
