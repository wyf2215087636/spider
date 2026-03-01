package com.spider.apigateway.auth.dto;

public record LoginResponse(
        String token,
        UserProfile user
) {
}
