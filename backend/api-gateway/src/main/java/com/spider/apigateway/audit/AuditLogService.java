package com.spider.apigateway.audit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.spider.apigateway.audit.dto.AuditLogResponse;
import com.spider.apigateway.audit.dto.ProjectAuditFilterOptionResponse;
import com.spider.apigateway.audit.dto.WorkspaceAuditFilterOptionResponse;
import com.spider.apigateway.project.ProjectEntity;
import com.spider.apigateway.project.ProjectMapper;
import com.spider.apigateway.workspace.WorkspaceEntity;
import com.spider.apigateway.workspace.WorkspaceMapper;
import com.spider.common.request.RequestContext;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;
    private final WorkspaceMapper workspaceMapper;
    private final ProjectMapper projectMapper;

    public AuditLogService(
            AuditLogMapper auditLogMapper,
            WorkspaceMapper workspaceMapper,
            ProjectMapper projectMapper
    ) {
        this.auditLogMapper = auditLogMapper;
        this.workspaceMapper = workspaceMapper;
        this.projectMapper = projectMapper;
    }

    public void recordSuccess(String action, String resourceType, String resourceId, String details) {
        record(action, resourceType, resourceId, "success", details);
    }

    public List<AuditLogResponse> list(String resourceType, String resourceId, Integer limit) {
        int finalLimit = limit == null ? 50 : Math.min(Math.max(limit, 1), 200);
        QueryWrapper<AuditLogEntity> query = new QueryWrapper<>();
        if (resourceType != null && !resourceType.isBlank()) {
            query.eq("resource_type", resourceType.trim());
        }
        if (resourceId != null && !resourceId.isBlank()) {
            query.eq("resource_id", resourceId.trim());
        }
        query.orderByDesc("created_at");
        query.last("LIMIT " + finalLimit);

        return auditLogMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    public List<WorkspaceAuditFilterOptionResponse> listWorkspaceFilterOptions() {
        QueryWrapper<WorkspaceEntity> workspaceQuery = new QueryWrapper<>();
        workspaceQuery.orderByDesc("created_at");
        List<WorkspaceEntity> activeWorkspaces = workspaceMapper.selectList(workspaceQuery);
        Map<String, WorkspaceAuditFilterOptionResponse> merged = new LinkedHashMap<>();

        for (WorkspaceEntity workspace : activeWorkspaces) {
            String id = workspace.getId().toString();
            merged.put(id, new WorkspaceAuditFilterOptionResponse(id, workspace.getName(), false));
        }

        QueryWrapper<AuditLogEntity> auditQuery = new QueryWrapper<>();
        auditQuery.eq("resource_type", "workspace");
        auditQuery.orderByDesc("created_at");
        List<AuditLogEntity> auditLogs = auditLogMapper.selectList(auditQuery);

        for (AuditLogEntity auditLog : auditLogs) {
            String resourceId = auditLog.getResourceId();
            if (resourceId == null || resourceId.isBlank()) {
                continue;
            }
            if (merged.containsKey(resourceId)) {
                continue;
            }
            String name = extractName(auditLog.getDetails());
            if (name == null || name.isBlank()) {
                name = resourceId;
            }
            merged.put(resourceId, new WorkspaceAuditFilterOptionResponse(resourceId, name, true));
        }

        return merged.values().stream().toList();
    }

    public List<ProjectAuditFilterOptionResponse> listProjectFilterOptions() {
        QueryWrapper<ProjectEntity> projectQuery = new QueryWrapper<>();
        projectQuery.orderByDesc("created_at");
        List<ProjectEntity> activeProjects = projectMapper.selectList(projectQuery);
        Map<String, ProjectAuditFilterOptionResponse> merged = new LinkedHashMap<>();

        for (ProjectEntity project : activeProjects) {
            String id = project.getId().toString();
            merged.put(id, new ProjectAuditFilterOptionResponse(id, project.getName(), false));
        }

        QueryWrapper<AuditLogEntity> auditQuery = new QueryWrapper<>();
        auditQuery.eq("resource_type", "project");
        auditQuery.orderByDesc("created_at");
        List<AuditLogEntity> auditLogs = auditLogMapper.selectList(auditQuery);

        for (AuditLogEntity auditLog : auditLogs) {
            String resourceId = auditLog.getResourceId();
            if (resourceId == null || resourceId.isBlank()) {
                continue;
            }
            if (merged.containsKey(resourceId)) {
                continue;
            }
            String name = extractName(auditLog.getDetails());
            if (name == null || name.isBlank()) {
                name = resourceId;
            }
            merged.put(resourceId, new ProjectAuditFilterOptionResponse(resourceId, name, true));
        }

        return merged.values().stream().toList();
    }

    private void record(String action, String resourceType, String resourceId, String status, String details) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setRequestId(RequestContext.getRequestId());
        entity.setActor(RequestContext.getActor());
        entity.setAction(action);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setStatus(status);
        entity.setDetails(details);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        auditLogMapper.insert(entity);
    }

    private AuditLogResponse toResponse(AuditLogEntity entity) {
        return new AuditLogResponse(
                entity.getId().toString(),
                entity.getRequestId(),
                entity.getActor(),
                entity.getAction(),
                entity.getResourceType(),
                entity.getResourceId(),
                entity.getStatus(),
                entity.getDetails(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString()
        );
    }

    private String extractName(String details) {
        if (details == null || details.isBlank()) {
            return null;
        }
        String trimmed = details.trim();
        if (trimmed.startsWith("name=")) {
            return trimmed.substring("name=".length()).trim();
        }
        return null;
    }
}
