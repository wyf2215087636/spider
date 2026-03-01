package com.spider.apigateway.exception;

import java.util.UUID;

public class ProductDocAccessDeniedException extends RuntimeException {
    private final UUID docId;

    public ProductDocAccessDeniedException(UUID docId) {
        this.docId = docId;
    }

    public UUID getDocId() {
        return docId;
    }
}
