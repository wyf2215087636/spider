package com.spider.apigateway.doc.dto;

public record ProductDocResponse(
        String id,
        String projectId,
        String title,
        String status,
        String ownerActor,
        String currentVersionId,
        Integer currentVersionNo,
        String createdAt,
        String updatedAt
) {
}
