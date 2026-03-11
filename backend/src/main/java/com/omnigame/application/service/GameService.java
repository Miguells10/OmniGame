package com.omnigame.application.service;

import com.omnigame.domain.model.Game;
import com.omnigame.infrastructure.persistence.GameEntityRepository;
import com.omnigame.infrastructure.persistence.GameRepository;
import com.omnigame.presentation.dto.GameResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service orchestrating game catalog operations.
 *
 * <p>Sits in the Application layer of the Hexagonal Architecture,
 * coordinating between domain entities and infrastructure repositories.
 * Handles pagination, search, and DTO mapping.</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final GameEntityRepository gameEntityRepository;

    /**
     * Lists all games with pagination, mapping each to a response DTO
     * with pre-computed entity counts.
     */
    public Page<GameResponse> listGames(Pageable pageable) {
        log.debug("Listing games with pageable: {}", pageable);
        return gameRepository.findAll(pageable)
                .map(game -> GameResponse.from(game, gameEntityRepository.countByGameId(game.getId())));
    }

    /**
     * Searches games by name or description with pagination.
     */
    public Page<GameResponse> searchGames(String query, Pageable pageable) {
        log.debug("Searching games with query='{}', pageable={}", query, pageable);
        return gameRepository.searchByNameOrDescription(query, pageable)
                .map(game -> GameResponse.from(game, gameEntityRepository.countByGameId(game.getId())));
    }

    /**
     * Retrieves a single game by its URL-friendly slug.
     *
     * @param slug unique slug identifier
     * @return game response DTO
     * @throws EntityNotFoundException if no game matches the slug
     */
    public GameResponse getBySlug(String slug) {
        log.debug("Fetching game by slug: {}", slug);
        Game game = gameRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + slug));
        long entityCount = gameEntityRepository.countByGameId(game.getId());
        return GameResponse.from(game, entityCount);
    }

    /**
     * Creates a new game in the catalog.
     *
     * @param game the game domain entity to persist
     * @return response DTO of the created game
     */
    @Transactional
    public GameResponse createGame(Game game) {
        log.info("Creating new game: name='{}', slug='{}'", game.getName(), game.getSlug());
        if (gameRepository.existsBySlug(game.getSlug())) {
            throw new IllegalArgumentException("Game with slug '" + game.getSlug() + "' already exists");
        }
        Game saved = gameRepository.save(game);
        return GameResponse.from(saved, 0L);
    }

    /**
     * Resolves a Game domain entity by slug, throwing if not found.
     * Used internally by other services (e.g., CollectorRagService).
     */
    public Game resolveBySlug(String slug) {
        return gameRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + slug));
    }
}
