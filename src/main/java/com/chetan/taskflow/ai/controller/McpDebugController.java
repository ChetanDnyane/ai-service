package com.chetan.taskflow.ai.controller;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
public class McpDebugController {

    private final ToolCallbackProvider toolCallbackProvider;

    public McpDebugController(
            ToolCallbackProvider toolCallbackProvider) {

        this.toolCallbackProvider = toolCallbackProvider;
    }

    /*
     * Lists all tools discovered through MCP.
     */
    @GetMapping("/tools")
    public List<String> getTools() {

        return Arrays.stream(
                        toolCallbackProvider.getToolCallbacks()
                )
                .map(toolCallback ->
                        toolCallback
                                .getToolDefinition()
                                .name())
                .toList();
    }

    /*
     * Directly invokes the remotely discovered
     * getTasks MCP tool.
     *
     * No LLM is involved here.
     */
    @GetMapping("/test-get-tasks")
    public String testGetTasks() {

        ToolCallback getTasksTool =
                Arrays.stream(
                                toolCallbackProvider
                                        .getToolCallbacks()
                        )
                        .filter(toolCallback ->
                                "getTasks".equals(
                                        toolCallback
                                                .getToolDefinition()
                                                .name()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "getTasks MCP tool was not found"
                                )
                        );

        /*
         * getTasks() has no arguments, therefore
         * its JSON argument object is simply {}.
         */
        String arguments = "{}";

        ToolContext toolContext =
                new ToolContext(Map.of());

        return getTasksTool.call(
                arguments,
                toolContext
        );
    }
}