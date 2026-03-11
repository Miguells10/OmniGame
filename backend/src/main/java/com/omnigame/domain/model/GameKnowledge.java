package com.omnigame.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores a chunk of knowledge with its vector embedding for RAG retrieval.
 *
 * <p>This entity powers The Collector's semantic search. Each row contains a
 * text chunk harvested from game wikis, documentation, or community forums,
 * along with its embedding vector (1536D for OpenAI text-embedding-3-small).
 * The pgvector extension enables efficient cosine-similarity searches.</p>
 *
 * <p><strong>Pipeline:</strong> The Harvester agent ingests raw content →
 * chunks it → generates embeddings via Spring AI → persists here.
 * The Collector agent queries this table during RAG to augment LLM prompts.</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Entity
@Table(name = "game_knowledge", indexes = {
        @Index(name = "idx_game_knowledge_game_id", columnList = "game_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    /**
     * The raw text chunk used for context augmentation in The Collector's RAG pipeline.
     */
    @Column(name = "context_chunk", nullable = false, columnDefinition = "TEXT")
    private String contextChunk;

    /**
     * Vector embedding (1536 dimensions) stored via pgvector.
     * Mapped as a float array in Java; persisted using the pgvector
     * {@code vector} column type in PostgreSQL.
     *
     * <p>Note: The actual pgvector type mapping is handled via a native
     * column definition. Hibernate treats this as a generic column.</p>
     */
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "chunk_metadata", columnDefinition = "TEXT")
    private String chunkMetadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
