package com.omnigame.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for The Collector RAG chatbot interactions.
 *
 * <p>Captures the user's message within the context of a specific game,
 * along with optional conversation history for multi-turn dialogue.</p>
 *
 * @param gameSlug            slug of the game context for RAG scoping
 * @param message             the user's current question or request
 * @param conversationHistory previous messages for multi-turn context (optional)
 */
public record ChatRequest(
        @NotBlank(message = "Game slug is required for context-aware responses")
        String gameSlug,

        @NotBlank(message = "Message cannot be empty")
        @Size(max = 2000, message = "Message must be under 2000 characters")
        String message,

        List<ChatMessage> conversationHistory
) {

    /**
     * Represents a single message in the conversation history.
     *
     * @param role    either "user" or "assistant"
     * @param content the message text
     */
    public record ChatMessage(String role, String content) {}
}
