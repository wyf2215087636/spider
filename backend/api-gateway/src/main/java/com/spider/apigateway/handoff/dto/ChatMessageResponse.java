package com.spider.apigateway.handoff.dto;

public record ChatMessageResponse(
        String id,
        String sessionId,
        String role,
        String content,
        String createdBy,
        String createdAt
) {
}
