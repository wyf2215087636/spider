package com.spider.apigateway.exception;

import java.util.UUID;

public class ProductDocInvalidRollbackVersionException extends RuntimeException {
    private final UUID docId;
    private final UUID versionId;

    public ProductDocInvalidRollbackVersionException(UUID docId, UUID versionId) {
        this.docId = docId;
        this.versionId = versionId;
    }

    public UUID getDocId() {
        return docId;
    }

    public UUID getVersionId() {
        return versionId;
    }
}
