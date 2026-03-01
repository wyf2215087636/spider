package com.spider.apigateway.exception;

import java.util.UUID;

public class ChatSessionNotFoundException extends RuntimeException {
    private final UUID chatSessionId;

    public ChatSessionNotFoundException(UUID chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public UUID getChatSessionId() {
        return chatSessionId;
    }
}
