package com.chetan.taskflow.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prompt")
public class PromptEngineeringController {

    private final ChatClient chatClient;

    public PromptEngineeringController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping("/classify")
    public String classify(@RequestBody PromptRequest request) {

        return chatClient
                .prompt()
                .system("""
                        You are a TaskFlow task classifier.

                        Classify the user's request into exactly one:
                        CREATE_TASK
                        UPDATE_TASK
                        DELETE_TASK
                        READ_TASK
                        UNKNOWN

                        Return only the classification.
                        """)
                .user(request.message())
                .call()
                .content();
    }

    public record PromptRequest(String message) {}
}