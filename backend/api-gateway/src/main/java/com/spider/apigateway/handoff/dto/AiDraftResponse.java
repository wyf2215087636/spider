package com.spider.apigateway.handoff.dto;

import java.util.List;

public record AiDraftResponse(
        String title,
        String requirementSummary,
        String acceptanceCriteria,
        String impactScope,
        String priority,
        String targetRole,
        List<String> suggestedTasks,
        List<String> impactedFiles,
        List<String> riskHints,
        List<String> testHints,
        String aiReply,
        String provider,
        String model
) {
}
