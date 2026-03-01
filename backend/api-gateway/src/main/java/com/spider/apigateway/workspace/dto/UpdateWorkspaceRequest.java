package com.spider.apigateway.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateWorkspaceRequest {
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "owner is required")
    private String owner;
    @NotBlank(message = "status is required")
    private String status;
    @NotBlank(message = "defaultLanguage is required")
    @Pattern(regexp = "^(zh-CN|en-US)$", message = "defaultLanguage must be zh-CN or en-US")
    private String defaultLanguage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}
