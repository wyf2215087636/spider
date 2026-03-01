package com.spider.apigateway.handoff;

import com.spider.apigateway.handoff.dto.AiDraftResponse;
import com.spider.apigateway.handoff.dto.ChatMessageResponse;
import com.spider.apigateway.handoff.dto.ChatSessionResponse;
import com.spider.apigateway.handoff.dto.CreateChatMessageRequest;
import com.spider.apigateway.handoff.dto.CreateChatSessionRequest;
import com.spider.apigateway.handoff.dto.GenerateAiDraftRequest;
import com.spider.apigateway.handoff.dto.PublishRequirementHandoffRequest;
import com.spider.apigateway.handoff.dto.RequirementHandoffResponse;
import com.spider.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat-sessions")
public class ChatSessionController {
    private final HandoffService handoffService;

    public ChatSessionController(HandoffService handoffService) {
        this.handoffService = handoffService;
    }

    @PostMapping
    public ApiResponse<ChatSessionResponse> create(@Valid @RequestBody CreateChatSessionRequest request) {
        return ApiResponse.success(handoffService.createSession(request));
    }

    @GetMapping
    public ApiResponse<List<ChatSessionResponse>> list(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String role
    ) {
        return ApiResponse.success(handoffService.listSessions(projectId, role));
    }

    @GetMapping("/{sessionId}/messages")
    public ApiResponse<List<ChatMessageResponse>> listMessages(@PathVariable UUID sessionId) {
        return ApiResponse.success(handoffService.listMessages(sessionId));
    }

    @PostMapping("/{sessionId}/messages")
    public ApiResponse<ChatMessageResponse> sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CreateChatMessageRequest request
    ) {
        return ApiResponse.success(handoffService.sendMessage(sessionId, request));
    }

    @PostMapping("/{sessionId}/ai-draft")
    public ApiResponse<AiDraftResponse> generateAiDraft(
            @PathVariable UUID sessionId,
            @Valid @RequestBody GenerateAiDraftRequest request
    ) {
        return ApiResponse.success(handoffService.generateAiDraft(sessionId, request));
    }

    @PostMapping("/{sessionId}/publish")
    public ApiResponse<RequirementHandoffResponse> publish(
            @PathVariable UUID sessionId,
            @Valid @RequestBody PublishRequirementHandoffRequest request
    ) {
        return ApiResponse.success(handoffService.publish(sessionId, request));
    }
}
