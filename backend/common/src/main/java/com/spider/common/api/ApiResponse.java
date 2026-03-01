package com.spider.common.api;

import com.spider.common.request.RequestContext;

import java.time.Instant;

public record ApiResponse<T>(
        String code,
        String message,
        T data,
        String timestamp,
        String requestId
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("0", "OK", data, Instant.now().toString(), RequestContext.getRequestId());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now().toString(), RequestContext.getRequestId());
    }
}
