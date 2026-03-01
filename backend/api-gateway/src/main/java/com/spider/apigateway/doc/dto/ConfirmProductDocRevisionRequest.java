package com.spider.apigateway.doc.dto;

import jakarta.validation.constraints.Size;

public class ConfirmProductDocRevisionRequest {
    @Size(max = 500, message = "changeSummary length must be <= 500")
    private String changeSummary;

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
}
