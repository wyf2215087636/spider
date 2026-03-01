package com.spider.apigateway.exception;

import java.util.UUID;

public class ProductDocRevisionNotFoundException extends RuntimeException {
    private final UUID revisionId;

    public ProductDocRevisionNotFoundException(UUID revisionId) {
        this.revisionId = revisionId;
    }

    public UUID getRevisionId() {
        return revisionId;
    }
}
