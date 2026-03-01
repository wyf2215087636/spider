package com.spider.apigateway.project.dto;

public record ProjectResponse(
        String id,
        String workspaceId,
        String name,
        String status,
        String createdAt,
        String updatedAt
) {
}
