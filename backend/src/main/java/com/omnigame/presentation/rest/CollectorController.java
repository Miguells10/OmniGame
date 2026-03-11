package com.omnigame.presentation.rest;

import com.omnigame.application.service.CollectorRagService;
import com.omnigame.presentation.dto.ChatRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for The Collector — OmniGame's RAG-powered AI chatbot.
 *
 * <p>Streams AI responses via Server-Sent Events (SSE) for real-time
 * token-by-token delivery to the frontend chat widget.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code POST /api/v1/collector/chat} — SSE streaming RAG chat</li>
 * </ul>
 *
 * @author OmniGame Engineering
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/v1/collector")
@RequiredArgsConstructor
@Slf4j
public class CollectorController {

    private final CollectorRagService ragService;

    /**
     * Processes a chat request and streams the AI response token-by-token.
     *
     * <p>The response is delivered as a {@code text/event-stream} SSE connection.
     * The frontend should consume this with an EventSource or fetch stream reader.</p>
     *
     * @param request chat request containing game context, message, and history
     * @return streaming flux of response text tokens
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Collector chat request — game='{}', messageLen={}",
                request.gameSlug(), request.message().length());

        // Convert DTO conversation history to Spring AI Message objects
        List<Message> history = List.of();
        if (request.conversationHistory() != null && !request.conversationHistory().isEmpty()) {
            history = request.conversationHistory().stream()
                    .map(msg -> switch (msg.role()) {
                        case "assistant" -> (Message) new AssistantMessage(msg.content());
                        case "user" -> (Message) new UserMessage(msg.content());
                        default -> (Message) new UserMessage(msg.content());
                    })
                    .collect(Collectors.toList());
        }

        return ragService.chat(request.gameSlug(), request.message(), history);
    }
}
