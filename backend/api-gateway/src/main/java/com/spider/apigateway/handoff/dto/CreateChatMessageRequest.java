package com.spider.apigateway.handoff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateChatMessageRequest {
    @NotBlank(message = "content is required")
    @Size(max = 8000, message = "content must be <= 8000 chars")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
