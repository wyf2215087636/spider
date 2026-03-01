package com.spider.apigateway.exception;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {
    private final UUID projectId;

    public ProjectNotFoundException(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getProjectId() {
        return projectId;
    }
}
