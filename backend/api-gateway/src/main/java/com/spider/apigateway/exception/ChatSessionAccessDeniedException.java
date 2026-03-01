package com.spider.apigateway.exception;

import java.util.UUID;

public class ChatSessionAccessDeniedException extends RuntimeException {
    private final UUID chatSessionId;

    public ChatSessionAccessDeniedException(UUID chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public UUID getChatSessionId() {
        return chatSessionId;
    }
}
