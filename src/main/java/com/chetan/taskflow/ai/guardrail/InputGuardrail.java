package com.chetan.taskflow.ai.guardrail;

import org.springframework.stereotype.Component;

@Component
public class InputGuardrail {

    private static final int MAX_LENGTH = 2000;

    public void validate(String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt cannot be empty."
            );
        }

        if (input.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Prompt exceeds maximum allowed length."
            );
        }
    }
}