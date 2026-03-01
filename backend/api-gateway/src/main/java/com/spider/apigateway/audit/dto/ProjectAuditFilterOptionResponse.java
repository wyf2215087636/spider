package com.spider.apigateway.audit.dto;

public record ProjectAuditFilterOptionResponse(
        String resourceId,
        String name,
        boolean deleted
) {
}
