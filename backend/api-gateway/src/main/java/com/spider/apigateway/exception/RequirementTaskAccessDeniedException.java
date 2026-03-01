package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementTaskAccessDeniedException extends RuntimeException {
    private final UUID taskId;

    public RequirementTaskAccessDeniedException(UUID taskId) {
        this.taskId = taskId;
    }

    public UUID getTaskId() {
        return taskId;
    }
}
