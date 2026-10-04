package com.chetan.taskflow.ai.controller;

import com.chetan.taskflow.ai.structured.TaskIntent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/structured")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(
            ChatClient.Builder chatClientBuilder) {

        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/task-intent")
    public TaskIntent extractTaskIntent(
            @RequestBody TaskIntentRequest request) {

        return chatClient
                .prompt()
                .system("""
                        Extract TaskFlow task information from the
                        user's request.

                        intent must be one of:
                        CREATE_TASK
                        UPDATE_TASK
                        DELETE_TASK
                        UNKNOWN

                        priority must be one of:
                        LOW
                        MEDIUM
                        HIGH

                        If no due date is specified, dueDate should
                        be null.

                        Do not invent information that the user
                        did not provide.
                        """)
                .user(request.message())
                .call()
                .entity(TaskIntent.class);
    }

    public record TaskIntentRequest(
            String message
    ) {
    }
}