package com.spider.apigateway.handoff;

import com.spider.apigateway.handoff.dto.RequirementTaskDetailResponse;
import com.spider.apigateway.handoff.dto.TaskCenterPageResponse;
import com.spider.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskCenterController {
    private final HandoffService handoffService;

    public TaskCenterController(HandoffService handoffService) {
        this.handoffService = handoffService;
    }

    @GetMapping
    public ApiResponse<TaskCenterPageResponse> list(
            @RequestParam(defaultValue = "pool") String view,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.success(handoffService.listTasksForCenter(view, projectId, status, role, page, size));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<RequirementTaskDetailResponse> detail(@PathVariable UUID taskId) {
        return ApiResponse.success(handoffService.getTaskDetail(taskId));
    }
}
