package com.chetan.taskflow.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private final ChatClient chatClient;

    public MemoryController(
            @Qualifier("memoryChatClient")
            ChatClient chatClient) {

        this.chatClient = chatClient;
    }

    @PostMapping("/chat")
    public String chat(
            @RequestParam String conversationId,
            @RequestBody ChatRequest request) {

        return chatClient
                .prompt()
                .user(request.message())
                .advisors(advisorSpec ->
                        advisorSpec.param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId
                        )
                )
                .call()
                .content();
    }

    public record ChatRequest(
            String message
    ) {
    }
}