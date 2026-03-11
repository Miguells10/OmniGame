package com.omnigame.infrastructure.persistence;

import com.omnigame.domain.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Game} aggregate roots.
 *
 * <p>Provides standard CRUD plus slug-based lookup and full-text search
 * across game names and descriptions.</p>
 */
@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    Optional<Game> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Game> searchByNameOrDescription(@Param("query") String query, Pageable pageable);
}
