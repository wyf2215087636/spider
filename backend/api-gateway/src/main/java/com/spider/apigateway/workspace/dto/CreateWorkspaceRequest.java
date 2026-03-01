package com.spider.apigateway.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateWorkspaceRequest {
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "owner is required")
    private String owner;
    @NotBlank(message = "defaultLanguage is required")
    @Pattern(regexp = "^(zh-CN|en-US)$", message = "defaultLanguage must be zh-CN or en-US")
    private String defaultLanguage = "zh-CN";

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

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}
