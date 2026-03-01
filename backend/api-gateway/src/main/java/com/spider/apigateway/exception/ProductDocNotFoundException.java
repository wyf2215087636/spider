package com.spider.apigateway.exception;

import java.util.UUID;

public class ProductDocNotFoundException extends RuntimeException {
    private final UUID docId;

    public ProductDocNotFoundException(UUID docId) {
        this.docId = docId;
    }

    public UUID getDocId() {
        return docId;
    }
}
