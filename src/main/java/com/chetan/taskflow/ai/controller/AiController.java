package com.chetan.taskflow.ai.controller;

import com.chetan.taskflow.ai.dto.AiRequest;
import com.chetan.taskflow.ai.dto.AiResponse;
import com.chetan.taskflow.ai.dto.TaskSuggestion;
import com.chetan.taskflow.ai.tool.TaskTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient chatClient;
    private final TaskTools taskTools;

    public AiController(
            ChatClient.Builder chatClientBuilder,
            TaskTools taskTools) {

        this.chatClient = chatClientBuilder.build();
        this.taskTools = taskTools;
    }

    @PostMapping("/ask")
    public AiResponse ask(@RequestBody AiRequest request) {

        String answer = chatClient
                .prompt()
                .system("""
                        You are the AI assistant for TaskFlow,
                        a personal task management application.

                        Help users understand, organize and prioritize their tasks.
                        Keep your answers concise and practical.
                        """)
                .user(request.message())
                .tools(taskTools)
                .call()
                .content();

        return new AiResponse(answer);
    }

    @PostMapping("/suggest-task")
    public TaskSuggestion suggestTask(@RequestBody AiRequest request) {

        LocalDate today = LocalDate.now();

        return chatClient
                .prompt()
                .system("""
                    You are the AI assistant for TaskFlow.

                    Extract a task from the user's request.

                    Today's date is %s.

                    The priority must be one of:
                    LOW, MEDIUM, HIGH.

                    Interpret relative dates such as today,
                    tomorrow and next week using today's date.
                    """.formatted(today))
                .user(request.message())
                .call()
                .entity(TaskSuggestion.class);
    }
}