package com.spider.apigateway.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateProjectRequest {
    @NotBlank(message = "workspaceId is required")
    private String workspaceId;
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "status is required")
    @Pattern(regexp = "^(draft|active|archived)$", message = "status must be draft/active/archived")
    private String status = "draft";

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
