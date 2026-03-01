package com.spider.apigateway.controller;

import com.spider.apigateway.dto.HealthResponse;
import com.spider.common.api.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Locale;

@RestController
public class HealthController {
    private final MessageSource messageSource;

    public HealthController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("/api/v1/health")
    public ApiResponse<HealthResponse> health(Locale locale) {
        String message = messageSource.getMessage("health.ok", null, locale);
        HealthResponse data = new HealthResponse("UP", Instant.now().toString(), message);
        return ApiResponse.success(data);
    }
}
