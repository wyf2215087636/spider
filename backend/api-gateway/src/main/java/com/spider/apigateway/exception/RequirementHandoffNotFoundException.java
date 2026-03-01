package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementHandoffNotFoundException extends RuntimeException {
    private final UUID handoffId;

    public RequirementHandoffNotFoundException(UUID handoffId) {
        this.handoffId = handoffId;
    }

    public UUID getHandoffId() {
        return handoffId;
    }
}
