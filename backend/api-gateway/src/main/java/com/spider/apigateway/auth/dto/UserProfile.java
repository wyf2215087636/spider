package com.spider.apigateway.auth.dto;

public record UserProfile(
        String userId,
        String username,
        String displayName,
        String role
) {
}
