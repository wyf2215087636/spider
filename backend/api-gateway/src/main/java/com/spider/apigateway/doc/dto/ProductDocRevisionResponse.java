package com.spider.apigateway.doc.dto;

public record ProductDocRevisionResponse(
        String id,
        String docId,
        String sourceVersionId,
        String baseContent,
        String candidateContent,
        String instruction,
        String changeSummary,
        String status,
        String modelProvider,
        String modelName,
        String createdBy,
        String confirmedBy,
        String confirmedAt,
        String createdAt,
        String updatedAt
) {
}
