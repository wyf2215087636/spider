package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementHandoffInsufficientContentException extends RuntimeException {
    private final UUID handoffId;

    public RequirementHandoffInsufficientContentException(UUID handoffId) {
        this.handoffId = handoffId;
    }

    public UUID getHandoffId() {
        return handoffId;
    }
}
