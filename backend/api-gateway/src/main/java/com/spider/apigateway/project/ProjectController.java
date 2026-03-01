package com.spider.apigateway.project;

import com.spider.apigateway.project.dto.CreateProjectRequest;
import com.spider.apigateway.project.dto.ProjectResponse;
import com.spider.apigateway.project.dto.UpdateProjectRequest;
import com.spider.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ApiResponse<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        return ApiResponse.success(projectService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> list(@RequestParam(required = false) String workspaceId) {
        return ApiResponse.success(projectService.list(workspaceId));
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> get(@PathVariable UUID projectId) {
        return ApiResponse.success(projectService.get(projectId));
    }

    @PutMapping("/{projectId}")
    public ApiResponse<ProjectResponse> update(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ApiResponse.success(projectService.update(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Map<String, String>> delete(@PathVariable UUID projectId) {
        projectService.delete(projectId);
        return ApiResponse.success(Map.of("deleted", "true"));
    }
}
