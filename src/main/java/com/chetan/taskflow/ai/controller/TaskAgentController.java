package com.chetan.taskflow.ai.controller;

import com.chetan.taskflow.ai.agent.ApprovalTools;
import com.chetan.taskflow.ai.agent.PendingActionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class TaskAgentController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;
    private final ApprovalTools approvalTools;
    private final PendingActionService pendingActionService;

    public TaskAgentController(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider,
            ApprovalTools approvalTools,
            PendingActionService pendingActionService) {

        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProvider = toolCallbackProvider;
        this.approvalTools = approvalTools;
        this.pendingActionService = pendingActionService;
    }

    @PostMapping("/run")
    public String runAgent(
            @RequestBody AgentRequest request,
            HttpServletRequest httpRequest) {

        // =====================================================
        // 1. Get JWT from incoming request
        // =====================================================

        String authorizationHeader =
                httpRequest.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new IllegalArgumentException(
                    "Authorization Bearer token is required"
            );
        }

        // =====================================================
        // 2. Create trusted ToolContext
        // =====================================================

        /*
         * The JWT is NOT sent in:
         *
         * - the user prompt
         * - the system prompt
         * - an LLM tool argument
         *
         * It is application-controlled security context.
         */
        Map<String, Object> toolContext =
                Map.of(
                        "authorization",
                        authorizationHeader
                );

        // =====================================================
        // 3. Obtain MCP tools but REMOVE deleteTask
        // =====================================================

        /*
         * This is the actual HITL security boundary.
         *
         * deleteTask exists on the MCP server, but we deliberately
         * do NOT expose it to this agent.
         *
         * Therefore the LLM cannot directly delete a task.
         */
        ToolCallback[] safeMcpTools =
                Arrays.stream(
                                toolCallbackProvider
                                        .getToolCallbacks()
                        )
                        .filter(tool ->
                                !"deleteTask".equals(
                                        tool
                                                .getToolDefinition()
                                                .name()
                                )
                        )
                        .toArray(ToolCallback[]::new);

        // =====================================================
        // 4. Run agent
        // =====================================================

        return chatClient
                .prompt()
                .system("""
                        You are the TaskFlow Task Agent.

                        Your job is to achieve the user's goal using
                        the available TaskFlow tools.

                        GENERAL RULES

                        1. Inspect the user's current tasks when the
                           goal depends on existing task information.

                        2. Decide which tools are necessary to achieve
                           the user's goal.

                        3. Use tool results to determine subsequent
                           actions.

                        4. Never invent task IDs, titles, descriptions,
                           statuses, priorities or due dates.

                        5. Never ask the user for authentication tokens,
                           user IDs or other security credentials.

                        6. Only modify tasks when modification is
                           necessary to achieve the user's explicit goal.

                        7. After completing a goal, briefly explain
                           what actions you performed.


                        DELETION / HUMAN APPROVAL RULES

                        8. You cannot directly delete TaskFlow tasks.

                        9. If the user's goal requires deleting a task,
                           use the requestTaskDeletion tool.

                        10. requestTaskDeletion does NOT delete the task.
                            It only creates a pending action requiring
                            human approval.

                        11. Never claim that a task has been deleted
                            merely because requestTaskDeletion succeeded.

                        12. When approval is required, clearly tell the
                            user that the deletion has NOT happened yet
                            and provide the approval ID returned by the
                            tool.

                        13. Do not attempt to bypass the human approval
                            process.
                        """)
                .user(request.goal())

                // Remote MCP tools, excluding deleteTask
                .toolCallbacks(safeMcpTools)

                // Local safe tool that creates approval requests
                .tools(approvalTools)

                // Trusted security context
                .toolContext(toolContext)

                .call()
                .content();
    }

    @PostMapping("/approve/{approvalId}")
    public String approve(
            @PathVariable String approvalId,
            HttpServletRequest httpRequest) {

        String authorizationHeader =
                httpRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new IllegalArgumentException(
                    "Authorization Bearer token is required"
            );
        }

        PendingActionService.PendingAction action =
                pendingActionService.get(approvalId);

        if (action.actionType()
                != PendingActionService.ActionType.DELETE_TASK) {

            throw new IllegalStateException(
                    "Unsupported pending action"
            );
        }

        /*
         * Execute the deletion directly against Task Service.
         *
         * The JWT comes from THIS approval request,
         * not from the LLM and not from the pending action.
         */
        RestClient restClient =
                RestClient.builder()
                        .baseUrl("http://localhost:8081")
                        .build();

        restClient
                .delete()
                .uri(
                        "/api/tasks/{taskId}",
                        action.taskId()
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .toBodilessEntity();

        /*
         * Consume the approval.
         *
         * The same approval ID cannot be used again.
         */
        pendingActionService.remove(approvalId);

        return "Task "
                + action.taskId()
                + " deleted successfully after human approval.";
    }
    
    // =========================================================
    // REQUEST DTO
    // =========================================================

    public record AgentRequest(
            String goal
    ) {
    }
}