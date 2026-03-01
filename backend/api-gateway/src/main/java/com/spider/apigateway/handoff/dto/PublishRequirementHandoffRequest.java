package com.spider.apigateway.handoff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PublishRequirementHandoffRequest {
    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be <= 200 chars")
    private String title;
    @NotBlank(message = "requirementSummary is required")
    private String requirementSummary;
    @NotBlank(message = "acceptanceCriteria is required")
    private String acceptanceCriteria;
    private String impactScope;
    @NotBlank(message = "priority is required")
    @Pattern(regexp = "^(P0|P1|P2|P3)$", message = "priority must be P0/P1/P2/P3")
    private String priority = "P2";
    @NotBlank(message = "targetRole is required")
    @Pattern(regexp = "^(product|backend|frontend|test)$", message = "targetRole must be product/backend/frontend/test")
    private String targetRole;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequirementSummary() {
        return requirementSummary;
    }

    public void setRequirementSummary(String requirementSummary) {
        this.requirementSummary = requirementSummary;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public String getImpactScope() {
        return impactScope;
    }

    public void setImpactScope(String impactScope) {
        this.impactScope = impactScope;
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
