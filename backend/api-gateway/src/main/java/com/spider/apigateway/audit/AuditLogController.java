package com.spider.apigateway.audit;

import com.spider.apigateway.audit.dto.AuditLogResponse;
import com.spider.apigateway.audit.dto.ProjectAuditFilterOptionResponse;
import com.spider.apigateway.audit.dto.WorkspaceAuditFilterOptionResponse;
import com.spider.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> list(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(auditLogService.list(resourceType, resourceId, limit));
    }

    @GetMapping("/workspace-options")
    public ApiResponse<List<WorkspaceAuditFilterOptionResponse>> listWorkspaceOptions() {
        return ApiResponse.success(auditLogService.listWorkspaceFilterOptions());
    }

    @GetMapping("/project-options")
    public ApiResponse<List<ProjectAuditFilterOptionResponse>> listProjectOptions() {
        return ApiResponse.success(auditLogService.listProjectFilterOptions());
    }
}
