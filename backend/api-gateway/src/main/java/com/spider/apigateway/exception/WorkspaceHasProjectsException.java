package com.spider.apigateway.exception;

import java.util.UUID;

public class WorkspaceHasProjectsException extends RuntimeException {
    private final UUID workspaceId;

    public WorkspaceHasProjectsException(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }
}
