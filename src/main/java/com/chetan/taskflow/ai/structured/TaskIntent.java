package com.chetan.taskflow.ai.structured;

import java.time.LocalDate;

public record TaskIntent(
        String intent,
        String title,
        String priority,
        LocalDate dueDate
) {
}