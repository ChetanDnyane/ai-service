package com.chetan.taskflow.ai.agent;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingActionService {

    private final Map<String, PendingAction> pendingActions =
            new ConcurrentHashMap<>();

    public PendingAction createDeleteAction(Long taskId) {

        String approvalId = UUID.randomUUID().toString();

        PendingAction action = new PendingAction(
                approvalId,
                ActionType.DELETE_TASK,
                taskId
        );

        pendingActions.put(approvalId, action);

        return action;
    }

    public PendingAction get(String approvalId) {

        PendingAction action = pendingActions.get(approvalId);

        if (action == null) {
            throw new IllegalArgumentException(
                    "Invalid or expired approval ID: " + approvalId
            );
        }

        return action;
    }

    public void remove(String approvalId) {
        pendingActions.remove(approvalId);
    }

    public enum ActionType {
        DELETE_TASK
    }

    public record PendingAction(
            String approvalId,
            ActionType actionType,
            Long taskId
    ) {
    }
}