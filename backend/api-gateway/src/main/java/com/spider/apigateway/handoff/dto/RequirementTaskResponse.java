package com.spider.apigateway.handoff.dto;

public record RequirementTaskResponse(
        String id,
        String handoffId,
        String projectId,
        String role,
        String title,
        String titleZh,
        String titleEn,
        String description,
        String descriptionZh,
        String descriptionEn,
        Integer estimateHours,
        String status,
        String assignee,
        String source,
        Integer sortOrder,
        String createdAt,
        String updatedAt
) {
}
