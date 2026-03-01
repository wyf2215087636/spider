package com.spider.apigateway.handoff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TransitionRequirementHandoffRequest {
    @NotBlank(message = "action is required")
    @Pattern(
            regexp = "^(submit_review|publish|accept|start_dev|start_test|complete|reject|reopen)$",
            message = "unsupported transition action"
    )
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
