package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementTaskNotFoundException extends RuntimeException {
    private final UUID taskId;

    public RequirementTaskNotFoundException(UUID taskId) {
        this.taskId = taskId;
    }

    public UUID getTaskId() {
        return taskId;
    }
}
