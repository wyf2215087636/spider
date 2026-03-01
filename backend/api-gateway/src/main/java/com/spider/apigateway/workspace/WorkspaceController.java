package com.spider.apigateway.workspace;

import com.spider.apigateway.workspace.dto.CreateWorkspaceRequest;
import com.spider.apigateway.workspace.dto.UpdateWorkspaceRequest;
import com.spider.apigateway.workspace.dto.WorkspaceResponse;
import com.spider.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ApiResponse<WorkspaceResponse> create(@Valid @RequestBody CreateWorkspaceRequest request) {
        return ApiResponse.success(workspaceService.create(request));
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> list() {
        return ApiResponse.success(workspaceService.list());
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> get(@PathVariable UUID workspaceId) {
        return ApiResponse.success(workspaceService.get(workspaceId));
    }

    @PutMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> update(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request
    ) {
        return ApiResponse.success(workspaceService.update(workspaceId, request));
    }

    @DeleteMapping("/{workspaceId}")
    public ApiResponse<Map<String, String>> delete(@PathVariable UUID workspaceId) {
        workspaceService.delete(workspaceId);
        return ApiResponse.success(Map.of("deleted", "true"));
    }
}
