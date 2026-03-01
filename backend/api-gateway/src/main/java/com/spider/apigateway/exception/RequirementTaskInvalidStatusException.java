package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementTaskInvalidStatusException extends RuntimeException {
    private final UUID taskId;
    private final String status;

    public RequirementTaskInvalidStatusException(UUID taskId, String status) {
        this.taskId = taskId;
        this.status = status;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }
}
