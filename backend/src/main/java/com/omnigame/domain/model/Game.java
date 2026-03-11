package com.omnigame.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Root aggregate representing a supported game in the OmniGame catalog.
 *
 * <p>Each game acts as the top-level grouping for all entities (mods, patches,
 * assets, tools) and knowledge vectors used by The Collector RAG agent.
 * The {@code slug} field provides a URL-friendly unique identifier.</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Entity
@Table(name = "games", indexes = {
        @Index(name = "idx_games_slug", columnList = "slug", unique = true),
        @Index(name = "idx_games_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GameEntity> entities = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GameKnowledge> knowledgeEntries = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Domain Behavior ──────────────────────────────────────────

    /**
     * Associates a new entity (mod/patch/asset/tool) with this game.
     */
    public void addEntity(GameEntity entity) {
        entities.add(entity);
        entity.setGame(this);
    }

    /**
     * Adds a knowledge vector chunk for RAG retrieval.
     */
    public void addKnowledge(GameKnowledge knowledge) {
        knowledgeEntries.add(knowledge);
        knowledge.setGame(this);
    }
}
