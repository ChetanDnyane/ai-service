package com.chetan.taskflow.ai.controller;

import com.chetan.taskflow.ai.guardrail.InputGuardrail;
import com.chetan.taskflow.ai.guardrail.TaskIntentGuardrail;
import com.chetan.taskflow.ai.structured.TaskIntent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/structured")
public class StructuredOutputController {

    private final ChatClient chatClient;
    private final InputGuardrail inputGuardrail;
    private final TaskIntentGuardrail taskIntentGuardrail;

    public StructuredOutputController(
            ChatClient.Builder chatClientBuilder,
            InputGuardrail inputGuardrail,
            TaskIntentGuardrail taskIntentGuardrail) {

        this.chatClient = chatClientBuilder.build();
        this.inputGuardrail = inputGuardrail;
        this.taskIntentGuardrail = taskIntentGuardrail;
    }

    @PostMapping("/task-intent")
    public TaskIntent extractTaskIntent(
            @RequestBody TaskIntentRequest request) {

        // INPUT GUARDRAIL
        inputGuardrail.validate(request.message());

        TaskIntent intent =
                chatClient
                        .prompt()
                        .system("""
                            Extract TaskFlow task information from
                            the user's request.

                            intent must be one of:
                            CREATE_TASK
                            UPDATE_TASK
                            DELETE_TASK
                            UNKNOWN

                            priority must be one of:
                            LOW
                            MEDIUM
                            HIGH

                            If priority is not specified,
                            return null.

                            If no due date is specified,
                            return null.

                            Do not invent information.
                            """)
                        .user(request.message())
                        .call()
                        .entity(TaskIntent.class);

        // OUTPUT GUARDRAIL
        taskIntentGuardrail.validate(intent);

        return intent;
    }

    public record TaskIntentRequest(
            String message
    ) {
    }
}