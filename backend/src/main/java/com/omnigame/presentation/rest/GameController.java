package com.omnigame.presentation.rest;

import com.omnigame.application.service.GameService;
import com.omnigame.domain.model.Game;
import com.omnigame.presentation.dto.GameResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the game catalog API.
 *
 * <p>Exposes endpoints for listing, searching, and managing games.
 * Follows RESTful conventions with proper pagination support
 * and HTTP status codes.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET  /api/v1/games}          — paginated game listing</li>
 *   <li>{@code GET  /api/v1/games/search}   — full-text search</li>
 *   <li>{@code GET  /api/v1/games/{slug}}   — single game by slug</li>
 *   <li>{@code POST /api/v1/games}          — create new game (admin)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    /**
     * Lists all games with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated list of game response DTOs
     */
    @GetMapping
    public ResponseEntity<Page<GameResponse>> listGames(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(gameService.listGames(pageable));
    }

    /**
     * Searches games by name or description.
     *
     * @param query    search query string
     * @param pageable pagination parameters
     * @return paginated search results
     */
    @GetMapping("/search")
    public ResponseEntity<Page<GameResponse>> searchGames(
            @RequestParam @NotBlank String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(gameService.searchGames(query, pageable));
    }

    /**
     * Retrieves a single game by its URL-friendly slug.
     *
     * @param slug unique slug identifier
     * @return game response DTO
     */
    @GetMapping("/{slug}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String slug) {
        return ResponseEntity.ok(gameService.getBySlug(slug));
    }

    /**
     * Creates a new game in the catalog. Requires admin authentication.
     *
     * @param request game creation request body
     * @return created game response DTO
     */
    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
        Game game = Game.builder()
                .name(request.name())
                .slug(request.slug())
                .coverUrl(request.coverUrl())
                .description(request.description())
                .build();
        GameResponse response = gameService.createGame(game);
        log.info("Game created successfully: slug='{}'", response.slug());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Request body for game creation.
     */
    public record CreateGameRequest(
            @NotBlank String name,
            @NotBlank String slug,
            String coverUrl,
            String description
    ) {}
}
