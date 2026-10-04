package com.chetan.taskflow.ai.guardrail;

import com.chetan.taskflow.ai.structured.TaskIntent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TaskIntentGuardrail {

    private static final Set<String> ALLOWED_INTENTS =
            Set.of(
                    "CREATE_TASK",
                    "UPDATE_TASK",
                    "DELETE_TASK",
                    "READ_TASK",
                    "UNKNOWN"
            );

    private static final Set<String> ALLOWED_PRIORITIES =
            Set.of(
                    "LOW",
                    "MEDIUM",
                    "HIGH"
            );

    public void validate(TaskIntent intent) {

        if (intent == null) {
            throw new IllegalArgumentException(
                    "LLM returned no task intent."
            );
        }

        if (!ALLOWED_INTENTS.contains(intent.intent())) {
            throw new IllegalArgumentException(
                    "Invalid intent returned by LLM: "
                            + intent.intent()
            );
        }

        if (intent.priority() != null
                && !ALLOWED_PRIORITIES.contains(
                intent.priority()
        )) {

            throw new IllegalArgumentException(
                    "Invalid priority returned by LLM: "
                            + intent.priority()
            );
        }
    }
}