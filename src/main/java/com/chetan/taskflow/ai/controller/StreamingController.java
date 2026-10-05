package com.chetan.taskflow.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream")
public class StreamingController {

    private final ChatClient chatClient;

    public StreamingController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping(
            value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> chat(
            @RequestBody ChatRequest request) {

        return chatClient
                .prompt()
                .user(request.message())
                .stream()
                .content();
    }

    public record ChatRequest(String message) {
    }
}