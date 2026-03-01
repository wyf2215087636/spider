package com.spider.apigateway.exception;

import java.util.UUID;

public class WorkspaceNotFoundException extends RuntimeException {
    private final UUID workspaceId;

    public WorkspaceNotFoundException(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }
}
