package com.spider.apigateway.doc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RollbackProductDocRequest {
    @NotBlank(message = "targetVersionId is required")
    private String targetVersionId;

    @Size(max = 500, message = "changeSummary length must be <= 500")
    private String changeSummary;

    public String getTargetVersionId() {
        return targetVersionId;
    }

    public void setTargetVersionId(String targetVersionId) {
        this.targetVersionId = targetVersionId;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
}
