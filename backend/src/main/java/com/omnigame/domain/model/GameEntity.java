package com.omnigame.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a downloadable entity (mod, patch, asset, or tool) within a game's catalog.
 *
 * <p>Each entity belongs to exactly one {@link Game} and carries a dynamic set of
 * metadata through the EAV pattern via {@link EntityValue} associations.
 * This design allows every game to define its own attribute schema
 * (e.g., "Load Order" for Skyrim, "Forge Version" for Minecraft) without
 * requiring schema migrations.</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Entity
@Table(name = "game_entities", indexes = {
        @Index(name = "idx_game_entities_game_id", columnList = "game_id"),
        @Index(name = "idx_game_entities_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false, length = 300)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntityType type;

    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "author_name", length = 200)
    private String authorName;

    @Column(name = "download_count")
    @Builder.Default
    private Long downloadCount = 0L;

    @Column(name = "security_audited")
    @Builder.Default
    private Boolean securityAudited = false;

    @OneToMany(mappedBy = "gameEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EntityValue> attributeValues = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // ── Domain Behavior ──────────────────────────────────────────

    /**
     * Attaches an EAV attribute-value pair to this entity.
     */
    public void addAttributeValue(EntityValue value) {
        attributeValues.add(value);
        value.setGameEntity(this);
    }

    /**
     * Marks this entity as having passed the Security Auditor agent scan.
     */
    public void markAsAudited() {
        this.securityAudited = true;
    }
}
