package com.spider.apigateway.dto;

public record HealthResponse(
        String status,
        String serverTime,
        String message
) {
}
