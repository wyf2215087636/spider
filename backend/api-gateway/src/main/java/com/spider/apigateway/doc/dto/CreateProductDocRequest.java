package com.spider.apigateway.doc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateProductDocRequest {
    @NotBlank(message = "projectId is required")
    private String projectId;

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title length must be <= 200")
    private String title;

    private String initialContent;

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

    public String getInitialContent() {
        return initialContent;
    }

    public void setInitialContent(String initialContent) {
        this.initialContent = initialContent;
    }
}
