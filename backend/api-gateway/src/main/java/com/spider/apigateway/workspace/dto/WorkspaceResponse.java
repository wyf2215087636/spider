package com.spider.apigateway.workspace.dto;

public record WorkspaceResponse(
        String id,
        String name,
        String owner,
        String status,
        String defaultLanguage,
        String createdAt,
        String updatedAt
) {
}
