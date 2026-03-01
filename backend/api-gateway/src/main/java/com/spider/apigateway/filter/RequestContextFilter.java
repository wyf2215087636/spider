package com.spider.apigateway.filter;

import com.spider.common.request.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component("spiderRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String ACTOR_HEADER = "X-Actor";

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
        String actor = request.getHeader(ACTOR_HEADER);
        if (actor == null || actor.isBlank()) {
            actor = "system";
        }

        RequestContext.setRequestId(requestId);
        RequestContext.setActor(actor);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/api/")) {
                long duration = System.currentTimeMillis() - start;
                log.info(
                        "requestId={} actor={} {} {} status={} durationMs={}",
                        requestId,
                        actor,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration
                );
            }
            RequestContext.clear();
        }
    }
}
