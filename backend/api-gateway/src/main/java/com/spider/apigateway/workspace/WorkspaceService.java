package com.spider.apigateway.workspace;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.spider.apigateway.audit.AuditLogService;
import com.spider.apigateway.exception.WorkspaceNotFoundException;
import com.spider.apigateway.workspace.dto.CreateWorkspaceRequest;
import com.spider.apigateway.workspace.dto.UpdateWorkspaceRequest;
import com.spider.apigateway.workspace.dto.WorkspaceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceService {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceService.class);
    private final WorkspaceMapper workspaceMapper;
    private final AuditLogService auditLogService;

    public WorkspaceService(WorkspaceMapper workspaceMapper, AuditLogService auditLogService) {
        this.workspaceMapper = workspaceMapper;
        this.auditLogService = auditLogService;
    }

    public WorkspaceResponse create(CreateWorkspaceRequest request) {
        log.info("workspace.create start name={} owner={}", request.getName(), request.getOwner());
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(request.getName().trim());
        entity.setOwner(request.getOwner().trim());
        entity.setStatus("active");
        entity.setDefaultLanguage(request.getDefaultLanguage());
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        workspaceMapper.insert(entity);
        log.info("workspace.create success workspaceId={}", entity.getId());
        auditLogService.recordSuccess(
                "workspace.create",
                "workspace",
                entity.getId().toString(),
                "name=" + entity.getName()
        );
        return toResponse(entity);
    }

    public List<WorkspaceResponse> list() {
        QueryWrapper<WorkspaceEntity> query = new QueryWrapper<>();
        query.orderByDesc("created_at");
        return workspaceMapper.selectList(query).stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkspaceResponse get(UUID workspaceId) {
        WorkspaceEntity entity = getById(workspaceId);
        return toResponse(entity);
    }

    public WorkspaceResponse update(UUID workspaceId, UpdateWorkspaceRequest request) {
        log.info("workspace.update start workspaceId={} status={}", workspaceId, request.getStatus());
        WorkspaceEntity entity = getById(workspaceId);
        entity.setName(request.getName().trim());
        entity.setOwner(request.getOwner().trim());
        entity.setStatus(request.getStatus().trim());
        entity.setDefaultLanguage(request.getDefaultLanguage());
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        workspaceMapper.updateById(entity);
        log.info("workspace.update success workspaceId={}", entity.getId());
        auditLogService.recordSuccess(
                "workspace.update",
                "workspace",
                entity.getId().toString(),
                "status=" + entity.getStatus()
        );
        return toResponse(entity);
    }

    public void delete(UUID workspaceId) {
        log.info("workspace.delete start workspaceId={}", workspaceId);
        WorkspaceEntity entity = getById(workspaceId);
        workspaceMapper.deleteById(entity.getId());
        log.info("workspace.delete success workspaceId={}", entity.getId());
        auditLogService.recordSuccess(
                "workspace.delete",
                "workspace",
                entity.getId().toString(),
                "name=" + entity.getName()
        );
    }

    private WorkspaceEntity getById(UUID workspaceId) {
        WorkspaceEntity entity = workspaceMapper.selectById(workspaceId);
        if (entity == null) {
            throw new WorkspaceNotFoundException(workspaceId);
        }
        return entity;
    }

    private WorkspaceResponse toResponse(WorkspaceEntity entity) {
        return new WorkspaceResponse(
                entity.getId().toString(),
                entity.getName(),
                entity.getOwner(),
                entity.getStatus(),
                entity.getDefaultLanguage(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }
}
