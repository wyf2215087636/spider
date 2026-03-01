package com.spider.apigateway.handoff.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GenerateAiDraftRequest {
    @Size(max = 4000, message = "requirementInput must be <= 4000 chars")
    private String requirementInput;

    @Pattern(regexp = "^(P0|P1|P2|P3)?$", message = "priority must be P0/P1/P2/P3")
    private String priority = "P2";

    @Pattern(
            regexp = "^(product|backend|frontend|test)?$",
            message = "targetRole must be product/backend/frontend/test"
    )
    private String targetRole = "backend";

    public String getRequirementInput() {
        return requirementInput;
    }

    public void setRequirementInput(String requirementInput) {
        this.requirementInput = requirementInput;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
