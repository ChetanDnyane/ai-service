package com.chetan.taskflow.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskTools {

    @Tool(description = "Returns the user's current TaskFlow tasks")
    public List<String> getTasks() {

        return List.of(
                "Learn Spring AI",
                "Prepare AWS architecture notes",
                "Update resume"
        );
    }
}