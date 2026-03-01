package com.spider.apigateway.auth;

import com.spider.apigateway.auth.dto.LoginRequest;
import com.spider.apigateway.auth.dto.LoginResponse;
import com.spider.apigateway.auth.dto.UserProfile;
import com.spider.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.logout(extractBearerToken(authorization));
        return ApiResponse.success("OK");
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        return ApiResponse.success(authService.me(extractBearerToken(authorization)));
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String raw = authorization.trim();
        if (raw.toLowerCase().startsWith("bearer ")) {
            return raw.substring("bearer ".length()).trim();
        }
        return raw;
    }
}
