package com.spider.apigateway.doc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateProductDocAiMessageRequest {
    @NotBlank(message = "content is required")
    @Size(max = 4000, message = "content length must be <= 4000")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
