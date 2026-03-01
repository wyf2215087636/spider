package com.spider.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.apigateway.auth.AuthPrincipal;
import com.spider.apigateway.auth.AuthService;
import com.spider.apigateway.exception.AuthUnauthorizedException;
import com.spider.common.api.ApiResponse;
import com.spider.common.request.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component("spiderRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public RequestContextFilter(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        response.setHeader(REQUEST_ID_HEADER, requestId);
        RequestContext.setRequestId(requestId);

        String actor = "anonymous";
        String role = "anonymous";
        String path = request.getRequestURI();
        boolean apiRequest = path.startsWith("/api/");
        boolean skipAuth = path.equals("/api/v1/health") || path.equals("/api/v1/auth/login");
        try {
            if (apiRequest && !skipAuth) {
                String token = extractBearerToken(request.getHeader(AUTHORIZATION_HEADER));
                AuthPrincipal principal = authService.resolvePrincipal(token);
                actor = principal.username();
                role = principal.role();
            }

            RequestContext.setRequestId(requestId);
            RequestContext.setActor(actor);
            RequestContext.setRole(role);

            long start = System.currentTimeMillis();
            filterChain.doFilter(request, response);
            if (apiRequest) {
                long duration = System.currentTimeMillis() - start;
                log.info(
                        "requestId={} actor={} role={} {} {} status={} durationMs={}",
                        requestId,
                        actor,
                        role,
                        request.getMethod(),
                        path,
                        response.getStatus(),
                        duration
                );
            }
        } catch (AuthUnauthorizedException ex) {
            if (apiRequest && !skipAuth) {
                log.warn(
                        "requestId={} {} {} unauthorized detail={}",
                        requestId,
                        request.getMethod(),
                        path,
                        ex.getMessage()
                );
                writeUnauthorized(response);
                return;
            }
            throw ex;
        } finally {
            RequestContext.clear();
        }
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

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> payload = ApiResponse.error("UNAUTHORIZED", "Unauthorized");
        response.getWriter().write(objectMapper.writeValueAsString(payload));
    }
}
