package com.spider.apigateway.exception;

import java.util.UUID;

public class ProductDocVersionNotFoundException extends RuntimeException {
    private final UUID versionId;

    public ProductDocVersionNotFoundException(UUID versionId) {
        this.versionId = versionId;
    }

    public UUID getVersionId() {
        return versionId;
    }
}
