package com.chetan.taskflow.ai.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ApprovalTools {

    private final PendingActionService pendingActionService;

    public ApprovalTools(
            PendingActionService pendingActionService) {

        this.pendingActionService = pendingActionService;
    }

    @Tool(description = """
            Request human approval to delete a TaskFlow task.

            Calling this tool DOES NOT delete the task.

            Use this whenever deletion is required.
            Never claim that the task has been deleted until
            approval has been granted and deletion has actually
            completed.
            """)
    public String requestTaskDeletion(Long taskId) {

        PendingActionService.PendingAction action =
                pendingActionService.createDeleteAction(taskId);

        return """
                Human approval required.

                Task ID: %d
                Approval ID: %s

                The task has NOT been deleted.
                """.formatted(
                taskId,
                action.approvalId()
        );
    }
}