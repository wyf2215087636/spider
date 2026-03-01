package com.spider.apigateway.auth;

import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        String username,
        String displayName,
        String role
) {
}
