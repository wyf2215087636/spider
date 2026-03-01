package com.spider.apigateway.doc.dto;

public record ProductDocDetailResponse(
        String id,
        String projectId,
        String title,
        String status,
        String ownerActor,
        String draftContent,
        String currentVersionId,
        Integer currentVersionNo,
        ProductDocVersionResponse currentVersion,
        String createdAt,
        String updatedAt
) {
}
