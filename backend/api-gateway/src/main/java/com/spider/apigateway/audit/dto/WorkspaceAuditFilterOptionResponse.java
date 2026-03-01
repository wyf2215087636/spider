package com.spider.apigateway.audit.dto;

public record WorkspaceAuditFilterOptionResponse(
        String resourceId,
        String name,
        boolean deleted
) {
}
