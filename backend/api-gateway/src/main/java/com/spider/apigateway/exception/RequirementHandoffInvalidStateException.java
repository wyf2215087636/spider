package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementHandoffInvalidStateException extends RuntimeException {
    private final UUID handoffId;
    private final String currentStatus;

    public RequirementHandoffInvalidStateException(UUID handoffId, String currentStatus) {
        this.handoffId = handoffId;
        this.currentStatus = currentStatus;
    }

    public UUID getHandoffId() {
        return handoffId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}
