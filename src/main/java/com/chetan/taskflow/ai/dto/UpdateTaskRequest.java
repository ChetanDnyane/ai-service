package com.chetan.taskflow.ai.dto;

import java.time.LocalDate;

public record UpdateTaskRequest(
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate
) {
}