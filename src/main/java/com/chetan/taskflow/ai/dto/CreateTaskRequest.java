package com.chetan.taskflow.ai.dto;

import java.time.LocalDate;

public record CreateTaskRequest(
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate
) {
}