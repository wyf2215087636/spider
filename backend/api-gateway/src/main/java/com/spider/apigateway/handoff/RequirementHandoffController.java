package com.spider.apigateway.handoff;

import com.spider.apigateway.handoff.dto.AiTaskPlanResponse;
import com.spider.apigateway.handoff.dto.RequirementTaskResponse;
import com.spider.apigateway.handoff.dto.RequirementHandoffResponse;
import com.spider.apigateway.handoff.dto.TransitionRequirementHandoffRequest;
import com.spider.apigateway.handoff.dto.UpdateRequirementTaskStatusRequest;
import com.spider.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/requirement-handoffs")
public class RequirementHandoffController {
    private final HandoffService handoffService;

    public RequirementHandoffController(HandoffService handoffService) {
        this.handoffService = handoffService;
    }

    @GetMapping
    public ApiResponse<List<RequirementHandoffResponse>> list(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String targetRole,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(handoffService.listHandoffs(projectId, targetRole, status));
    }

    @PostMapping("/{handoffId}/accept")
    public ApiResponse<RequirementHandoffResponse> accept(@PathVariable UUID handoffId) {
        return ApiResponse.success(handoffService.accept(handoffId));
    }

    @PostMapping("/{handoffId}/transition")
    public ApiResponse<RequirementHandoffResponse> transition(
            @PathVariable UUID handoffId,
            @Valid @RequestBody TransitionRequirementHandoffRequest request
    ) {
        return ApiResponse.success(handoffService.transitionHandoff(handoffId, request));
    }

    @PostMapping("/{handoffId}/tasks/ai-generate")
    public ApiResponse<AiTaskPlanResponse> generateAiTasks(@PathVariable UUID handoffId) {
        return ApiResponse.success(handoffService.generateAiTasks(handoffId));
    }

    @GetMapping("/{handoffId}/tasks")
    public ApiResponse<List<RequirementTaskResponse>> listTasks(@PathVariable UUID handoffId) {
        return ApiResponse.success(handoffService.listTasks(handoffId));
    }

    @PostMapping("/tasks/{taskId}/claim")
    public ApiResponse<RequirementTaskResponse> claimTask(@PathVariable UUID taskId) {
        return ApiResponse.success(handoffService.claimTask(taskId));
    }

    @PutMapping("/tasks/{taskId}/status")
    public ApiResponse<RequirementTaskResponse> updateTaskStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateRequirementTaskStatusRequest request
    ) {
        return ApiResponse.success(handoffService.updateTaskStatus(taskId, request));
    }
}
