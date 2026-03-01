package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementHandoffInvalidTransitionException extends RuntimeException {
    private final UUID handoffId;
    private final String action;
    private final String currentStatus;

    public RequirementHandoffInvalidTransitionException(UUID handoffId, String action, String currentStatus) {
        this.handoffId = handoffId;
        this.action = action;
        this.currentStatus = currentStatus;
    }

    public UUID getHandoffId() {
        return handoffId;
    }

    public String getAction() {
        return action;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}
