package com.spider.apigateway.handoff.dto;

public record RequirementTaskDetailResponse(
        String id,
        String handoffId,
        String projectId,
        String projectName,
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
        String updatedAt,
        String handoffTitle,
        String handoffRequirementSummary,
        String handoffAcceptanceCriteria,
        String handoffImpactScope,
        String handoffPriority,
        String handoffTargetRole,
        String handoffStatus
) {
}
