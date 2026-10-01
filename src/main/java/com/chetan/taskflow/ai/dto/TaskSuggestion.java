package com.chetan.taskflow.ai.dto;

import java.time.LocalDate;

public record TaskSuggestion(
        String title,
        Priority priority,
        LocalDate dueDate
) {
}