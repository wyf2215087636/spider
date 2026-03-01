package com.spider.apigateway.doc.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PublishProductDocVersionRequest {
    @Size(max = 500, message = "changeSummary length must be <= 500")
    private String changeSummary;

    @Pattern(
            regexp = "^(manual|ai_confirm)$",
            message = "sourceType must be manual/ai_confirm"
    )
    private String sourceType = "manual";

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
