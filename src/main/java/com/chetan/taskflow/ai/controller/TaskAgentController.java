package com.chetan.taskflow.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class TaskAgentController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;

    public TaskAgentController(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider) {

        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @PostMapping("/run")
    public String runAgent(
            @RequestBody AgentRequest request,
            HttpServletRequest httpRequest) {

        String authorizationHeader =
                httpRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new IllegalArgumentException(
                    "Authorization Bearer token is required"
            );
        }

        Map<String, Object> toolContext =
                Map.of(
                        "authorization",
                        authorizationHeader
                );

        return chatClient
                .prompt()
                .system("""
                        You are the TaskFlow Task Agent.

                        Your job is to achieve the user's goal using
                        the available TaskFlow tools.

                        Rules:

                        1. Inspect the user's current tasks when the
                           goal depends on existing task information.

                        2. Decide which tools are required.

                        3. Use tool results to determine subsequent
                           actions.

                        4. Never invent task IDs, task information,
                           task status, priority or due dates.

                        5. Never ask for or expose authentication
                           tokens or user IDs.

                        6. Only modify tasks when modification is
                           necessary to achieve the user's explicit goal.

                        7. After completing the goal, explain briefly
                           what actions you performed.
                        """)
                .user(request.goal())
                .toolCallbacks(
                        toolCallbackProvider.getToolCallbacks()
                )
                .toolContext(toolContext)
                .call()
                .content();
    }

    public record AgentRequest(
            String goal
    ) {
    }
}