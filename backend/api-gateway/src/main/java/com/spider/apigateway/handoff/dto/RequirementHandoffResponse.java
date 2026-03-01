package com.spider.apigateway.handoff.dto;

public record RequirementHandoffResponse(
        String id,
        String projectId,
        String sourceSessionId,
        Integer version,
        String title,
        String requirementSummary,
        String acceptanceCriteria,
        String impactScope,
        String priority,
        String targetRole,
        String status,
        String publishedBy,
        String publishedAt,
        String acceptedBy,
        String acceptedAt,
        String createdAt,
        String updatedAt
) {
}
