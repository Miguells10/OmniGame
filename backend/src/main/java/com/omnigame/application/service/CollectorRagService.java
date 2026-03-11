package com.omnigame.application.service;

import com.omnigame.domain.model.Game;
import com.omnigame.domain.model.GameKnowledge;
import com.omnigame.infrastructure.persistence.GameKnowledgeRepository;
import com.omnigame.infrastructure.persistence.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Collector — RAG-powered chatbot service for game modding support.
 *
 * <p>This service implements the core RAG (Retrieval-Augmented Generation) pipeline:
 * <ol>
 *   <li>Embed the user's query using Spring AI's EmbeddingModel</li>
 *   <li>Perform cosine-similarity search against game-scoped knowledge vectors</li>
 *   <li>Augment the LLM prompt with retrieved context chunks</li>
 *   <li>Stream the AI response via SSE (Server-Sent Events)</li>
 * </ol>
 * </p>
 *
 * <p><strong>Architecture Note:</strong> This service sits in the Application layer
 * and orchestrates between the Domain (GameKnowledge) and Infrastructure
 * (Spring AI ChatClient, EmbeddingModel) adapters.</p>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollectorRagService {

    private static final int TOP_K_RESULTS = 5;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are "The Collector", the expert AI assistant of OmniGame AI.
            You specialize in game modding, technical support, load order resolution,
            and compatibility troubleshooting for the game: %s.

            RULES:
            - Answer ONLY based on the provided CONTEXT. If the context doesn't contain
              enough information, say so honestly and suggest where the user might find help.
            - Be precise, technical, and actionable. Gamers want exact steps.
            - When discussing mod conflicts, always mention the specific file names and load orders.
            - Format your response with markdown for readability.
            - If the user asks about security, warn them about untrusted mod sources.

            CONTEXT (retrieved from the OmniGame knowledge base):
            ---
            %s
            ---
            """;

    private final GameRepository gameRepository;
    private final GameKnowledgeRepository knowledgeRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final EmbeddingModel embeddingModel;

    /**
     * Processes a RAG chat request and streams the AI response.
     *
     * @param gameSlug    slug of the game to scope the knowledge search
     * @param userMessage the user's query text
     * @param history     optional conversation history for multi-turn context
     * @return a Flux of string tokens streamed as the LLM generates its response
     */
    public Flux<String> chat(String gameSlug, String userMessage, List<Message> history) {
        log.info("The Collector activated — game='{}', query='{}'",
                gameSlug, userMessage.substring(0, Math.min(100, userMessage.length())));

        // 1. Resolve game
        Game game = gameRepository.findBySlug(gameSlug)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameSlug));

        // 2. Embed query
        float[] queryEmbedding = embeddingModel.embed(userMessage);
        String vectorString = formatVector(queryEmbedding);

        // 3. Retrieve relevant knowledge chunks
        List<GameKnowledge> relevantChunks = knowledgeRepository
                .findTopKBySemanticSimilarity(game.getId(), vectorString, TOP_K_RESULTS);

        String context = relevantChunks.stream()
                .map(GameKnowledge::getContextChunk)
                .collect(Collectors.joining("\n\n---\n\n"));

        if (context.isBlank()) {
            context = "No specific knowledge found for this game yet. " +
                      "The Harvester agent has not ingested content for " + game.getName() + ".";
        }

        log.debug("Retrieved {} knowledge chunks for RAG context", relevantChunks.size());

        // 4. Build augmented prompt with system context
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, game.getName(), context);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));

        // Add conversation history for multi-turn
        if (history != null) {
            messages.addAll(history);
        }

        messages.add(new UserMessage(userMessage));

        // 5. Stream LLM response via Spring AI ChatClient
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content();
    }

    /**
     * Formats a float array as a pgvector-compatible string.
     * Example: [0.1, 0.2, 0.3] → "[0.1,0.2,0.3]"
     */
    private String formatVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
