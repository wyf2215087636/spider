package com.spider.apigateway.exception;

import java.util.UUID;

public class ProductDocRevisionInvalidStateException extends RuntimeException {
    private final UUID revisionId;
    private final String currentStatus;

    public ProductDocRevisionInvalidStateException(UUID revisionId, String currentStatus) {
        this.revisionId = revisionId;
        this.currentStatus = currentStatus;
    }

    public UUID getRevisionId() {
        return revisionId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}
