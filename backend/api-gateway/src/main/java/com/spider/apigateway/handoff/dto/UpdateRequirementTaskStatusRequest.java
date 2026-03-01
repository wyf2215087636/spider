package com.spider.apigateway.handoff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateRequirementTaskStatusRequest {
    @NotBlank(message = "status is required")
    @Pattern(regexp = "^(todo|in_progress|done|blocked)$", message = "task status must be todo/in_progress/done/blocked")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
