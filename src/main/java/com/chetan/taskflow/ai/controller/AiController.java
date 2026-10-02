package com.chetan.taskflow.ai.controller;

import com.chetan.taskflow.ai.context.RequestContext;
import com.chetan.taskflow.ai.dto.AiRequest;
import com.chetan.taskflow.ai.dto.AiResponse;
import com.chetan.taskflow.ai.dto.TaskSuggestion;
import com.chetan.taskflow.ai.tool.TaskTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient chatClient;
    private final TaskTools taskTools;
    private final RequestContext requestContext;

    public AiController(
            ChatClient.Builder chatClientBuilder,
            TaskTools taskTools,
            RequestContext requestContext) {

        this.chatClient = chatClientBuilder.build();
        this.taskTools = taskTools;
        this.requestContext = requestContext;
    }

    @PostMapping("/ask")
    public AiResponse ask(
            @RequestBody AiRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        requestContext.setAuthorizationHeader(authorizationHeader);
        System.out.println(
                "Authorization header received: "
                        + (authorizationHeader != null)
        );

        String answer = chatClient
                .prompt()
                .system("""
                        You are the AI assistant for TaskFlow,
                        a personal task management application.

                        You can use the provided tools to work with the
                        authenticated user's tasks.

                        Use tools when the user asks about their actual tasks.
                        Never invent task information.

                        LOW, MEDIUM and HIGH are valid task priorities.

                        TODO, IN_PROGRESS and COMPLETED are valid task statuses.

                        When the user only asks for information, do not modify data.

                        For task creation, modification or deletion, only perform
                        the operation when the user's request clearly asks for it.

                        Keep responses concise and practical.
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