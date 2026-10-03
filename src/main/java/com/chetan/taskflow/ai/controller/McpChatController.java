package com.chetan.taskflow.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
public class McpChatController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;

    public McpChatController(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider) {

        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam String message,
            HttpServletRequest request) {

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new IllegalArgumentException(
                    "Authorization Bearer token is required"
            );
        }

        /*
         * IMPORTANT:
         *
         * This JWT is application/security context.
         *
         * It is NOT:
         *
         * - added to the user prompt
         * - added to the system prompt
         * - exposed as an LLM tool parameter
         *
         * Spring AI carries ToolContext into MCP _meta.
         */
        Map<String, Object> toolContext =
                Map.of(
                        "authorization",
                        authorizationHeader
                );

        return chatClient
                .prompt()
                .system("""
                        You are the TaskFlow AI assistant.

                        Use the available tools when the user asks
                        for information or operations involving
                        their TaskFlow tasks.

                        Never invent task information.

                        Never ask the user for authentication tokens,
                        user IDs, or other security credentials.
                        """)
                .user(message)
                .toolCallbacks(
                        toolCallbackProvider.getToolCallbacks()
                )
                .toolContext(toolContext)
                .call()
                .content();
    }
}