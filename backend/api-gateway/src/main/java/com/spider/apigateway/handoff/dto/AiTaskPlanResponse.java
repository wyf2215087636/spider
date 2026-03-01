package com.spider.apigateway.handoff.dto;

import java.util.List;

public record AiTaskPlanResponse(
        String handoffId,
        List<RequirementTaskResponse> tasks,
        List<String> impactedFiles,
        List<String> riskHints,
        List<String> testHints,
        String provider,
        String model,
        String rationale
) {
}
