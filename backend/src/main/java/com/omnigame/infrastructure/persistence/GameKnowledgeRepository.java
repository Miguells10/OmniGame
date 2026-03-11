package com.omnigame.infrastructure.persistence;

import com.omnigame.domain.model.GameKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link GameKnowledge} entities with pgvector semantic search.
 *
 * <p>The core of The Collector's RAG pipeline: performs cosine-similarity
 * vector searches against game-scoped knowledge embeddings.</p>
 */
@Repository
public interface GameKnowledgeRepository extends JpaRepository<GameKnowledge, UUID> {

    /**
     * Performs a cosine-similarity search against the knowledge base for a specific game.
     *
     * <p>Uses the pgvector {@code <=>} cosine distance operator. The query vector
     * must be cast to {@code vector} type. Results are ordered by ascending distance
     * (i.e., most similar first) and limited to {@code topK} results.</p>
     *
     * @param gameId      UUID of the target game
     * @param queryVector the embedding vector of the user's query (as a formatted string)
     * @param topK        maximum number of results to return
     * @return list of relevant knowledge chunks ordered by semantic similarity
     */
    @Query(value = """
            SELECT gk.* FROM game_knowledge gk
            WHERE gk.game_id = :gameId
            ORDER BY gk.embedding <=> CAST(:queryVector AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<GameKnowledge> findTopKBySemanticSimilarity(
            @Param("gameId") UUID gameId,
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );

    long countByGameId(UUID gameId);
}
