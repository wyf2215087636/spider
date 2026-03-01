package com.spider.apigateway.handoff.dto;

public record ChatSessionResponse(
        String id,
        String projectId,
        String ownerActor,
        String role,
        String title,
        String status,
        String createdAt,
        String updatedAt
) {
}
