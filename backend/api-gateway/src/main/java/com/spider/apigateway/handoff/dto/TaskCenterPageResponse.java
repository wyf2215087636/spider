package com.spider.apigateway.handoff.dto;

import java.util.List;

public record TaskCenterPageResponse(
        List<RequirementTaskResponse> items,
        long total,
        int page,
        int size,
        boolean hasNext
) {
}
