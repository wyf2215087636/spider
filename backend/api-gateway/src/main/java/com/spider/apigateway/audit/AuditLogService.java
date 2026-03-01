package com.spider.apigateway.audit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.spider.apigateway.audit.dto.AuditLogResponse;
import com.spider.common.request.RequestContext;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
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
}
