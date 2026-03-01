package com.spider.apigateway.doc.dto;

public record ProductDocVersionResponse(
        String id,
        String docId,
        Integer versionNo,
        String parentVersionId,
        String content,
        String changeSummary,
        String sourceType,
        String createdBy,
        String createdAt
) {
}
