package com.spider.apigateway.doc.dto;

public record ProductDocAiGenerateResponse(
        ProductDocAiMessageResponse assistantMessage,
        ProductDocRevisionResponse revision
) {
}
