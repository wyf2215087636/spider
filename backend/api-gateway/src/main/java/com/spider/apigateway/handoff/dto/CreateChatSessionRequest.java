package com.spider.apigateway.handoff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateChatSessionRequest {
    @NotBlank(message = "projectId is required")
    private String projectId;
    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be <= 200 chars")
    private String title;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
