package com.spider.apigateway.doc.dto;

public record ProductDocAiMessageResponse(
        String id,
        String docId,
        String role,
        String content,
        String createdBy,
        String createdAt
) {
}
