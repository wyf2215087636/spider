package com.spider.apigateway.doc.dto;

import jakarta.validation.constraints.Size;

public class RejectProductDocRevisionRequest {
    @Size(max = 500, message = "reason length must be <= 500")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
