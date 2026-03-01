package com.spider.apigateway.doc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProductDocRequest {
    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title length must be <= 200")
    private String title;

    @NotBlank(message = "draftContent is required")
    private String draftContent;

    @Pattern(
            regexp = "^(draft|active|archived)$",
            message = "status must be draft/active/archived"
    )
    private String status = "draft";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDraftContent() {
        return draftContent;
    }

    public void setDraftContent(String draftContent) {
        this.draftContent = draftContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
