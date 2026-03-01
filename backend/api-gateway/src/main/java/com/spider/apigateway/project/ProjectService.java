package com.spider.apigateway.project;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.spider.apigateway.audit.AuditLogService;
import com.spider.apigateway.exception.ProjectNotFoundException;
import com.spider.apigateway.exception.WorkspaceNotFoundException;
import com.spider.apigateway.project.dto.CreateProjectRequest;
import com.spider.apigateway.project.dto.ProjectResponse;
import com.spider.apigateway.project.dto.UpdateProjectRequest;
import com.spider.apigateway.workspace.WorkspaceEntity;
import com.spider.apigateway.workspace.WorkspaceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectMapper projectMapper;
    private final WorkspaceMapper workspaceMapper;
    private final AuditLogService auditLogService;

    public ProjectService(
            ProjectMapper projectMapper,
            WorkspaceMapper workspaceMapper,
            AuditLogService auditLogService
    ) {
        this.projectMapper = projectMapper;
        this.workspaceMapper = workspaceMapper;
        this.auditLogService = auditLogService;
    }

    public ProjectResponse create(CreateProjectRequest request) {
        UUID workspaceId = UUID.fromString(request.getWorkspaceId().trim());
        ensureWorkspaceExists(workspaceId);
        log.info("project.create start workspaceId={} name={}", workspaceId, request.getName());

        ProjectEntity entity = new ProjectEntity();
        entity.setId(UUID.randomUUID());
        entity.setWorkspaceId(workspaceId);
        entity.setName(request.getName().trim());
        entity.setStatus(request.getStatus().trim());
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        projectMapper.insert(entity);

        auditLogService.recordSuccess(
                "project.create",
                "project",
                entity.getId().toString(),
                "name=" + entity.getName()
        );
        log.info("project.create success projectId={}", entity.getId());
        return toResponse(entity);
    }

    public List<ProjectResponse> list(String workspaceId) {
        QueryWrapper<ProjectEntity> query = new QueryWrapper<>();
        if (workspaceId != null && !workspaceId.isBlank()) {
            query.eq("workspace_id", UUID.fromString(workspaceId.trim()));
        }
        query.orderByDesc("created_at");
        return projectMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    public ProjectResponse get(UUID projectId) {
        return toResponse(getById(projectId));
    }

    public ProjectResponse update(UUID projectId, UpdateProjectRequest request) {
        log.info("project.update start projectId={} status={}", projectId, request.getStatus());
        ProjectEntity entity = getById(projectId);
        entity.setName(request.getName().trim());
        entity.setStatus(request.getStatus().trim());
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        projectMapper.updateById(entity);
        auditLogService.recordSuccess(
                "project.update",
                "project",
                entity.getId().toString(),
                "status=" + entity.getStatus()
        );
        log.info("project.update success projectId={}", entity.getId());
        return toResponse(entity);
    }

    public void delete(UUID projectId) {
        log.info("project.delete start projectId={}", projectId);
        ProjectEntity entity = getById(projectId);
        projectMapper.deleteById(entity.getId());
        auditLogService.recordSuccess(
                "project.delete",
                "project",
                entity.getId().toString(),
                "name=" + entity.getName()
        );
        log.info("project.delete success projectId={}", entity.getId());
    }

    public int countByWorkspaceId(UUID workspaceId) {
        QueryWrapper<ProjectEntity> query = new QueryWrapper<>();
        query.eq("workspace_id", workspaceId);
        Long count = projectMapper.selectCount(query);
        return count == null ? 0 : count.intValue();
    }

    private ProjectEntity getById(UUID projectId) {
        ProjectEntity entity = projectMapper.selectById(projectId);
        if (entity == null) {
            throw new ProjectNotFoundException(projectId);
        }
        return entity;
    }

    private void ensureWorkspaceExists(UUID workspaceId) {
        WorkspaceEntity workspace = workspaceMapper.selectById(workspaceId);
        if (workspace == null) {
            throw new WorkspaceNotFoundException(workspaceId);
        }
    }

    private ProjectResponse toResponse(ProjectEntity entity) {
        return new ProjectResponse(
                entity.getId().toString(),
                entity.getWorkspaceId() == null ? null : entity.getWorkspaceId().toString(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }
}
