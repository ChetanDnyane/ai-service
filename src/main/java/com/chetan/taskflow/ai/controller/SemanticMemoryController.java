package com.chetan.taskflow.ai.controller;

import com.chetan.taskflow.ai.memory.SemanticMemoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/semantic-memory")
public class SemanticMemoryController {

    private final SemanticMemoryService semanticMemoryService;
    private final ChatClient chatClient;

    public SemanticMemoryController(
            SemanticMemoryService semanticMemoryService,
            ChatClient.Builder chatClientBuilder) {

        this.semanticMemoryService = semanticMemoryService;
        this.chatClient = chatClientBuilder.build();
    }

    // =========================================================
    // REMEMBER
    // =========================================================

    @PostMapping("/remember")
    public String remember(
            @RequestBody RememberRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        semanticMemoryService.remember(
                userId,
                request.memory()
        );

        return "Memory stored successfully.";
    }

    // =========================================================
    // CHAT USING LONG-TERM SEMANTIC MEMORY
    // =========================================================

    @PostMapping("/chat")
    public String chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        List<Document> memories =
                semanticMemoryService.recall(
                        userId,
                        request.message()
                );

        String memoryContext =
                memories.stream()
                        .map(Document::getText)
                        .collect(
                                Collectors.joining("\n")
                        );

        return chatClient
                .prompt()
                .system("""
                        You are a helpful assistant.

                        You may use the following long-term memories
                        about the authenticated user when they are
                        relevant to the question.

                        Do not invent additional memories.

                        If the memories are irrelevant, ignore them.

                        Long-term memories:

                        %s
                        """.formatted(memoryContext))
                .user(request.message())
                .call()
                .content();
    }

    // =========================================================
    // AUTHENTICATED USER
    // =========================================================

    private Long getUserId(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof Long userId)) {

            throw new IllegalStateException(
                    "Authenticated user ID is unavailable"
            );
        }

        return userId;
    }

    // =========================================================
    // REQUEST DTOs
    // =========================================================

    public record RememberRequest(
            String memory
    ) {
    }

    public record ChatRequest(
            String message
    ) {
    }
}