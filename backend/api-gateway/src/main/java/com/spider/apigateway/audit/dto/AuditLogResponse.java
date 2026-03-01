package com.spider.apigateway.audit.dto;

public record AuditLogResponse(
        String id,
        String requestId,
        String actor,
        String action,
        String resourceType,
        String resourceId,
        String status,
        String details,
        String createdAt
) {
}
