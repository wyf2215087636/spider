package com.spider.apigateway.exception;

import java.util.UUID;

public class RequirementTaskPlanGenerationException extends RuntimeException {
    private final UUID handoffId;
    private final String reason;

    public RequirementTaskPlanGenerationException(UUID handoffId, String reason) {
        this.handoffId = handoffId;
        this.reason = reason;
    }

    public UUID getHandoffId() {
        return handoffId;
    }

    public String getReason() {
        return reason;
    }
}
