package com.spider.apigateway.handoff;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.spider.apigateway.ai.AiDraftService;
import com.spider.apigateway.audit.AuditLogService;
import com.spider.apigateway.exception.ChatSessionAccessDeniedException;
import com.spider.apigateway.exception.ChatSessionNotFoundException;
import com.spider.apigateway.exception.ProjectNotFoundException;
import com.spider.apigateway.exception.AuthUnauthorizedException;
import com.spider.apigateway.exception.RequirementHandoffInvalidTransitionException;
import com.spider.apigateway.exception.RequirementHandoffInvalidStateException;
import com.spider.apigateway.exception.RequirementHandoffInsufficientContentException;
import com.spider.apigateway.exception.RequirementHandoffNotFoundException;
import com.spider.apigateway.exception.RequirementTaskAccessDeniedException;
import com.spider.apigateway.exception.RequirementTaskInvalidStatusException;
import com.spider.apigateway.exception.RequirementTaskNotFoundException;
import com.spider.apigateway.exception.RequirementTaskPlanGenerationException;
import com.spider.apigateway.handoff.dto.AiDraftResponse;
import com.spider.apigateway.handoff.dto.AiTaskPlanResponse;
import com.spider.apigateway.handoff.dto.ChatMessageResponse;
import com.spider.apigateway.handoff.dto.ChatSessionResponse;
import com.spider.apigateway.handoff.dto.CreateChatMessageRequest;
import com.spider.apigateway.handoff.dto.CreateChatSessionRequest;
import com.spider.apigateway.handoff.dto.GenerateAiDraftRequest;
import com.spider.apigateway.handoff.dto.PublishRequirementHandoffRequest;
import com.spider.apigateway.handoff.dto.RequirementTaskDetailResponse;
import com.spider.apigateway.handoff.dto.RequirementTaskResponse;
import com.spider.apigateway.handoff.dto.RequirementHandoffResponse;
import com.spider.apigateway.handoff.dto.TaskCenterPageResponse;
import com.spider.apigateway.handoff.dto.TransitionRequirementHandoffRequest;
import com.spider.apigateway.handoff.dto.UpdateRequirementTaskStatusRequest;
import com.spider.apigateway.project.ProjectEntity;
import com.spider.apigateway.project.ProjectMapper;
import com.spider.common.request.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class HandoffService {
    private static final Logger log = LoggerFactory.getLogger(HandoffService.class);
    private static final Set<String> SUPPORTED_CHAT_ROLES = Set.of("product", "backend", "frontend", "test");
    private static final Set<String> TASK_STATUSES = Set.of("todo", "in_progress", "done", "blocked");
    private static final Set<String> PRODUCT_ROLES = Set.of("product", "admin");
    private static final Set<String> DEV_ROLES = Set.of("backend", "frontend", "admin");
    private static final Set<String> TEST_ROLES = Set.of("test", "admin");
    private static final Set<String> PMO_ROLES = Set.of("pmo", "admin");
    private static final Set<String> TASK_OVERVIEW_ROLES = Set.of("product", "pmo", "admin");
    private static final Set<String> TASK_DOMAIN_ROLES = Set.of("product", "backend", "frontend", "test");
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final RequirementHandoffMapper requirementHandoffMapper;
    private final RequirementTaskMapper requirementTaskMapper;
    private final ProjectMapper projectMapper;
    private final AuditLogService auditLogService;
    private final AiDraftService aiDraftService;

    public HandoffService(
            ChatSessionMapper chatSessionMapper,
            ChatMessageMapper chatMessageMapper,
            RequirementHandoffMapper requirementHandoffMapper,
            RequirementTaskMapper requirementTaskMapper,
            ProjectMapper projectMapper,
            AuditLogService auditLogService,
            AiDraftService aiDraftService
    ) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.requirementHandoffMapper = requirementHandoffMapper;
        this.requirementTaskMapper = requirementTaskMapper;
        this.projectMapper = projectMapper;
        this.auditLogService = auditLogService;
        this.aiDraftService = aiDraftService;
    }

    public ChatSessionResponse createSession(CreateChatSessionRequest request) {
        UUID projectId = UUID.fromString(request.getProjectId().trim());
        ensureProjectExists(projectId);

        ChatSessionEntity entity = new ChatSessionEntity();
        String role = RequestContext.getRole();
        if (!SUPPORTED_CHAT_ROLES.contains(role)) {
            throw new AuthUnauthorizedException();
        }
        entity.setId(UUID.randomUUID());
        entity.setProjectId(projectId);
        entity.setOwnerActor(RequestContext.getActor());
        entity.setRole(role);
        entity.setTitle(request.getTitle().trim());
        entity.setStatus("active");
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        chatSessionMapper.insert(entity);
        appendMessage(entity.getId(), "system", "Private session created. You can now discuss requirements with AI.", "system");

        auditLogService.recordSuccess(
                "chat.session.create",
                "chat_session",
                entity.getId().toString(),
                "role=" + entity.getRole()
        );
        log.info(
                "chat.session.create success sessionId={} projectId={} ownerActor={}",
                entity.getId(),
                entity.getProjectId(),
                entity.getOwnerActor()
        );
        return toSessionResponse(entity);
    }

    public List<ChatSessionResponse> listSessions(String projectId, String role) {
        QueryWrapper<ChatSessionEntity> query = new QueryWrapper<>();
        query.eq("owner_actor", RequestContext.getActor());
        if (projectId != null && !projectId.isBlank()) {
            query.eq("project_id", UUID.fromString(projectId.trim()));
        }
        if (role != null && !role.isBlank()) {
            query.eq("role", role.trim());
        }
        query.orderByDesc("created_at");
        return chatSessionMapper.selectList(query).stream().map(this::toSessionResponse).toList();
    }

    public List<ChatMessageResponse> listMessages(UUID sessionId) {
        getOwnedSession(sessionId);
        QueryWrapper<ChatMessageEntity> query = new QueryWrapper<>();
        query.eq("session_id", sessionId);
        query.orderByAsc("created_at");
        return chatMessageMapper.selectList(query).stream().map(this::toMessageResponse).toList();
    }

    public ChatMessageResponse sendMessage(UUID sessionId, CreateChatMessageRequest request) {
        ChatSessionEntity session = getOwnedSession(sessionId);
        String prompt = request.getContent().trim();

        appendMessage(sessionId, "user", prompt, RequestContext.getActor());
        List<ChatMessageEntity> latestMessages = listLatestMessages(sessionId, 12);

        AiDraftResponse draft = aiDraftService.generateDraft(
                session,
                latestMessages,
                prompt,
                "backend",
                "P2"
        );
        String assistantReply = draft.aiReply();
        ChatMessageEntity assistantMessage = appendMessage(sessionId, "assistant", assistantReply, "ai-agent");

        auditLogService.recordSuccess(
                "chat.message.send",
                "chat_session",
                sessionId.toString(),
                "messageRole=user"
        );
        log.info("chat.message.send success sessionId={} actor={}", sessionId, RequestContext.getActor());
        return toMessageResponse(assistantMessage);
    }

    public AiDraftResponse generateAiDraft(UUID sessionId, GenerateAiDraftRequest request) {
        ChatSessionEntity session = getOwnedSession(sessionId);
        List<ChatMessageEntity> latestMessages = listLatestMessages(sessionId, 20);
        AiDraftResponse response = aiDraftService.generateDraft(
                session,
                latestMessages,
                request.getRequirementInput(),
                request.getTargetRole(),
                request.getPriority()
        );
        appendMessage(sessionId, "assistant", response.aiReply(), "ai-agent");

        auditLogService.recordSuccess(
                "requirement.handoff.aiDraft",
                "chat_session",
                sessionId.toString(),
                "targetRole=" + response.targetRole() + ";priority=" + response.priority()
        );
        log.info(
                "requirement.handoff.aiDraft success sessionId={} actor={} targetRole={} priority={}",
                sessionId,
                RequestContext.getActor(),
                response.targetRole(),
                response.priority()
        );
        return response;
    }

    public RequirementHandoffResponse publish(UUID sessionId, PublishRequirementHandoffRequest request) {
        ChatSessionEntity session = getOwnedSession(sessionId);

        QueryWrapper<RequirementHandoffEntity> versionQuery = new QueryWrapper<>();
        versionQuery.eq("source_session_id", sessionId);
        Long currentCount = requirementHandoffMapper.selectCount(versionQuery);
        int version = currentCount == null ? 1 : currentCount.intValue() + 1;

        RequirementHandoffEntity entity = new RequirementHandoffEntity();
        entity.setId(UUID.randomUUID());
        entity.setProjectId(session.getProjectId());
        entity.setSourceSessionId(session.getId());
        entity.setVersion(version);
        entity.setTitle(request.getTitle().trim());
        entity.setRequirementSummary(request.getRequirementSummary().trim());
        entity.setAcceptanceCriteria(request.getAcceptanceCriteria().trim());
        entity.setImpactScope(request.getImpactScope() == null ? null : request.getImpactScope().trim());
        entity.setPriority(request.getPriority().trim());
        entity.setTargetRole(request.getTargetRole().trim());
        entity.setStatus("draft");
        entity.setPublishedBy(RequestContext.getActor());
        entity.setPublishedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        requirementHandoffMapper.insert(entity);

        auditLogService.recordSuccess(
                "requirement.handoff.publish",
                "requirement_handoff",
                entity.getId().toString(),
                "status=draft;targetRole=" + entity.getTargetRole() + ";priority=" + entity.getPriority()
        );
        log.info(
                "requirement.handoff.createDraft success handoffId={} sessionId={} version={}",
                entity.getId(),
                sessionId,
                version
        );
        return toHandoffResponse(entity);
    }

    public List<RequirementHandoffResponse> listHandoffs(String projectId, String targetRole, String status) {
        QueryWrapper<RequirementHandoffEntity> query = new QueryWrapper<>();
        if (projectId != null && !projectId.isBlank()) {
            query.eq("project_id", UUID.fromString(projectId.trim()));
        }
        if (targetRole != null && !targetRole.isBlank()) {
            query.eq("target_role", targetRole.trim());
        }
        if (status != null && !status.isBlank()) {
            query.eq("status", status.trim());
        }
        query.orderByDesc("created_at");
        return requirementHandoffMapper.selectList(query).stream().map(this::toHandoffResponse).toList();
    }

    public RequirementHandoffResponse accept(UUID handoffId) {
        TransitionRequirementHandoffRequest request = new TransitionRequirementHandoffRequest();
        request.setAction("accept");
        return transitionHandoff(handoffId, request);
    }

    public RequirementHandoffResponse transitionHandoff(UUID handoffId, TransitionRequirementHandoffRequest request) {
        RequirementHandoffEntity entity = requirementHandoffMapper.selectById(handoffId);
        if (entity == null) {
            throw new RequirementHandoffNotFoundException(handoffId);
        }

        String action = request.getAction().trim();
        String currentStatus = entity.getStatus();
        String role = RequestContext.getRole();
        String nextStatus = resolveTransitionStatus(entity, action, currentStatus, role);

        entity.setStatus(nextStatus);
        if ("published".equals(nextStatus)) {
            entity.setPublishedBy(RequestContext.getActor());
            entity.setPublishedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        if ("accepted".equals(nextStatus)) {
            entity.setAcceptedBy(RequestContext.getActor());
            entity.setAcceptedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        requirementHandoffMapper.updateById(entity);

        auditLogService.recordSuccess(
                "requirement.handoff.transition",
                "requirement_handoff",
                entity.getId().toString(),
                "action=" + action + ";from=" + currentStatus + ";to=" + nextStatus
        );
        log.info(
                "requirement.handoff.transition success handoffId={} actor={} action={} from={} to={}",
                entity.getId(),
                RequestContext.getActor(),
                action,
                currentStatus,
                nextStatus
        );
        return toHandoffResponse(entity);
    }

    public List<RequirementTaskResponse> listTasks(UUID handoffId) {
        ensureHandoffExists(handoffId);
        QueryWrapper<RequirementTaskEntity> query = new QueryWrapper<>();
        query.eq("handoff_id", handoffId);
        String role = RequestContext.getRole();
        if (!TASK_OVERVIEW_ROLES.contains(role)) {
            query.eq("role", role);
        }
        query.orderByAsc("sort_order").orderByAsc("created_at");
        return requirementTaskMapper.selectList(query).stream().map(this::toTaskResponse).toList();
    }

    public RequirementTaskDetailResponse getTaskDetail(UUID taskId) {
        RequirementTaskEntity task = requirementTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RequirementTaskNotFoundException(taskId);
        }
        ensureTaskReadable(task);

        RequirementHandoffEntity handoff = requirementHandoffMapper.selectById(task.getHandoffId());
        if (handoff == null) {
            throw new RequirementHandoffNotFoundException(task.getHandoffId());
        }

        ProjectEntity project = projectMapper.selectById(task.getProjectId());
        String titleZh = isBlank(task.getTitleZh()) ? task.getTitle() : task.getTitleZh();
        String titleEn = isBlank(task.getTitleEn()) ? task.getTitle() : task.getTitleEn();
        String descriptionZh = isBlank(task.getDescriptionZh()) ? task.getDescription() : task.getDescriptionZh();
        String descriptionEn = isBlank(task.getDescriptionEn()) ? task.getDescription() : task.getDescriptionEn();

        return new RequirementTaskDetailResponse(
                task.getId().toString(),
                task.getHandoffId().toString(),
                task.getProjectId().toString(),
                project == null ? null : project.getName(),
                task.getRole(),
                task.getTitle(),
                titleZh,
                titleEn,
                task.getDescription(),
                descriptionZh,
                descriptionEn,
                task.getEstimateHours(),
                task.getStatus(),
                task.getAssignee(),
                task.getSource(),
                task.getSortOrder(),
                task.getCreatedAt() == null ? null : task.getCreatedAt().toString(),
                task.getUpdatedAt() == null ? null : task.getUpdatedAt().toString(),
                handoff.getTitle(),
                handoff.getRequirementSummary(),
                handoff.getAcceptanceCriteria(),
                handoff.getImpactScope(),
                handoff.getPriority(),
                handoff.getTargetRole(),
                handoff.getStatus()
        );
    }

    public TaskCenterPageResponse listTasksForCenter(
            String view,
            String projectId,
            String status,
            String roleFilter,
            Integer page,
            Integer size
    ) {
        String normalizedView = normalizeTaskView(view);
        String currentRole = RequestContext.getRole();
        String currentActor = RequestContext.getActor();
        String normalizedRoleFilter = normalizeTaskRoleFilter(roleFilter);
        boolean isPmoOrAdmin = PMO_ROLES.contains(currentRole);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);

        QueryWrapper<RequirementTaskEntity> query = new QueryWrapper<>();
        if (projectId != null && !projectId.isBlank()) {
            query.eq("project_id", UUID.fromString(projectId.trim()));
        }
        if (status != null && !status.isBlank()) {
            query.eq("status", status.trim());
        }

        if ("mine".equals(normalizedView)) {
            query.eq("assignee", currentActor);
            if (!isPmoOrAdmin) {
                query.eq("role", currentRole);
            } else if (normalizedRoleFilter != null) {
                query.eq("role", normalizedRoleFilter);
            }
        } else if ("all".equals(normalizedView)) {
            if (!isPmoOrAdmin) {
                throw new AuthUnauthorizedException();
            }
            if (normalizedRoleFilter != null) {
                query.eq("role", normalizedRoleFilter);
            }
        } else {
            query.isNull("assignee");
            if (!isPmoOrAdmin) {
                query.eq("role", currentRole);
            } else if (normalizedRoleFilter != null) {
                query.eq("role", normalizedRoleFilter);
            }
        }

        Long totalCount = requirementTaskMapper.selectCount(query);
        long total = totalCount == null ? 0L : totalCount;
        long offset = (long) (normalizedPage - 1) * normalizedSize;

        query.orderByDesc("updated_at").orderByDesc("created_at");
        query.last("LIMIT " + normalizedSize + " OFFSET " + Math.max(0, offset));
        List<RequirementTaskResponse> items = requirementTaskMapper.selectList(query).stream().map(this::toTaskResponse).toList();
        boolean hasNext = (long) normalizedPage * normalizedSize < total;
        return new TaskCenterPageResponse(items, total, normalizedPage, normalizedSize, hasNext);
    }

    public AiTaskPlanResponse generateAiTasks(UUID handoffId) {
        if (!PMO_ROLES.contains(RequestContext.getRole())) {
            throw new AuthUnauthorizedException();
        }
        RequirementHandoffEntity handoff = ensureHandoffExists(handoffId);
        if (!Set.of("published", "accepted").contains(handoff.getStatus())) {
            throw new RequirementHandoffInvalidStateException(handoffId, handoff.getStatus());
        }
        if (isBlank(handoff.getTitle())
                || isBlank(handoff.getRequirementSummary())
                || isBlank(handoff.getAcceptanceCriteria())) {
            throw new RequirementHandoffInsufficientContentException(handoffId);
        }

        QueryWrapper<RequirementTaskEntity> existingQuery = new QueryWrapper<>();
        existingQuery.eq("handoff_id", handoffId);
        List<RequirementTaskEntity> existing = requirementTaskMapper.selectList(existingQuery);
        if (!existing.isEmpty()) {
            return new AiTaskPlanResponse(
                    handoffId.toString(),
                    existing.stream().sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder())).map(this::toTaskResponse).toList(),
                    List.of(),
                    List.of(),
                    List.of(),
                    "existing",
                    "existing",
                    "Task plan already exists, reuse current tasks."
            );
        }

        AiDraftService.TaskPlanResult planResult = aiDraftService.generateTaskPlan(handoff);
        if (planResult.tasks().isEmpty()) {
            throw new RequirementTaskPlanGenerationException(handoffId, "empty_or_invalid_model_output");
        }
        List<RequirementTaskResponse> created = new ArrayList<>();
        int order = 0;
        for (AiDraftService.TaskPlanItem item : planResult.tasks()) {
            RequirementTaskEntity entity = new RequirementTaskEntity();
            entity.setId(UUID.randomUUID());
            entity.setHandoffId(handoffId);
            entity.setProjectId(handoff.getProjectId());
            entity.setRole(item.role());
            entity.setTitle(item.titleEn());
            entity.setTitleZh(item.titleZh());
            entity.setTitleEn(item.titleEn());
            entity.setDescription(item.descriptionEn());
            entity.setDescriptionZh(item.descriptionZh());
            entity.setDescriptionEn(item.descriptionEn());
            entity.setEstimateHours(item.estimateHours());
            entity.setStatus("todo");
            entity.setSource("ai");
            entity.setSortOrder(order++);
            entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            requirementTaskMapper.insert(entity);
            created.add(toTaskResponse(entity));
        }

        auditLogService.recordSuccess(
                "requirement.task.aiGenerate",
                "requirement_handoff",
                handoffId.toString(),
                "count=" + created.size()
        );
        log.info(
                "requirement.task.aiGenerate success handoffId={} count={} actor={}",
                handoffId,
                created.size(),
                RequestContext.getActor()
        );
        return new AiTaskPlanResponse(
                handoffId.toString(),
                created,
                planResult.impactedFiles(),
                planResult.riskHints(),
                planResult.testHints(),
                planResult.provider(),
                planResult.model(),
                planResult.rationale()
        );
    }

    public RequirementTaskResponse claimTask(UUID taskId) {
        RequirementTaskEntity entity = requirementTaskMapper.selectById(taskId);
        if (entity == null) {
            throw new RequirementTaskNotFoundException(taskId);
        }
        ensureTaskAccess(entity);
        if (entity.getAssignee() != null && !entity.getAssignee().isBlank()
                && !RequestContext.getActor().equals(entity.getAssignee())
                && !"admin".equals(RequestContext.getRole())) {
            throw new RequirementTaskAccessDeniedException(taskId);
        }
        entity.setAssignee(RequestContext.getActor());
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        requirementTaskMapper.updateById(entity);

        auditLogService.recordSuccess(
                "requirement.task.claim",
                "requirement_task",
                taskId.toString(),
                "assignee=" + entity.getAssignee()
        );
        return toTaskResponse(entity);
    }

    public RequirementTaskResponse updateTaskStatus(UUID taskId, UpdateRequirementTaskStatusRequest request) {
        RequirementTaskEntity entity = requirementTaskMapper.selectById(taskId);
        if (entity == null) {
            throw new RequirementTaskNotFoundException(taskId);
        }
        ensureTaskAccess(entity);
        String nextStatus = request.getStatus().trim();
        if (!TASK_STATUSES.contains(nextStatus)) {
            throw new RequirementTaskInvalidStatusException(taskId, nextStatus);
        }
        if (entity.getAssignee() == null || entity.getAssignee().isBlank()) {
            entity.setAssignee(RequestContext.getActor());
        } else if (!RequestContext.getActor().equals(entity.getAssignee()) && !"admin".equals(RequestContext.getRole())) {
            throw new RequirementTaskAccessDeniedException(taskId);
        }
        entity.setStatus(nextStatus);
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        requirementTaskMapper.updateById(entity);

        auditLogService.recordSuccess(
                "requirement.task.updateStatus",
                "requirement_task",
                taskId.toString(),
                "status=" + nextStatus + ";assignee=" + entity.getAssignee()
        );
        return toTaskResponse(entity);
    }

    private ChatSessionEntity getOwnedSession(UUID sessionId) {
        ChatSessionEntity entity = chatSessionMapper.selectById(sessionId);
        if (entity == null) {
            throw new ChatSessionNotFoundException(sessionId);
        }
        if (!RequestContext.getActor().equals(entity.getOwnerActor())) {
            throw new ChatSessionAccessDeniedException(sessionId);
        }
        return entity;
    }

    private List<ChatMessageEntity> listLatestMessages(UUID sessionId, int limit) {
        QueryWrapper<ChatMessageEntity> query = new QueryWrapper<>();
        query.eq("session_id", sessionId);
        query.orderByDesc("created_at");
        query.last("LIMIT " + Math.max(1, limit));
        List<ChatMessageEntity> list = chatMessageMapper.selectList(query);
        return list.stream().sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())).toList();
    }

    private ChatMessageEntity appendMessage(
            UUID sessionId,
            String messageRole,
            String content,
            String createdBy
    ) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(UUID.randomUUID());
        entity.setSessionId(sessionId);
        entity.setMessageRole(messageRole);
        entity.setContent(content);
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        chatMessageMapper.insert(entity);
        return entity;
    }

    private void ensureProjectExists(UUID projectId) {
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ProjectNotFoundException(projectId);
        }
    }

    private RequirementHandoffEntity ensureHandoffExists(UUID handoffId) {
        RequirementHandoffEntity entity = requirementHandoffMapper.selectById(handoffId);
        if (entity == null) {
            throw new RequirementHandoffNotFoundException(handoffId);
        }
        return entity;
    }

    private String resolveTransitionStatus(
            RequirementHandoffEntity entity,
            String action,
            String currentStatus,
            String role
    ) {
        return switch (action) {
            case "submit_review" -> {
                requireRole(entity.getId(), role, PRODUCT_ROLES, action, currentStatus);
                requireStatus(entity.getId(), action, currentStatus, Set.of("draft"));
                yield "in_review";
            }
            case "publish" -> {
                requireRole(entity.getId(), role, PRODUCT_ROLES, action, currentStatus);
                requireStatus(entity.getId(), action, currentStatus, Set.of("in_review"));
                yield "published";
            }
            case "accept" -> {
                requireStatus(entity.getId(), action, currentStatus, Set.of("published"));
                String targetRole = entity.getTargetRole() == null ? "" : entity.getTargetRole().trim();
                if (!"admin".equals(role) && !role.equals(targetRole)) {
                    throw new RequirementHandoffInvalidTransitionException(entity.getId(), action, currentStatus);
                }
                yield "accepted";
            }
            case "start_dev" -> {
                requireRole(entity.getId(), role, DEV_ROLES, action, currentStatus);
                requireStatus(entity.getId(), action, currentStatus, Set.of("accepted"));
                yield "in_development";
            }
            case "start_test" -> {
                requireRole(entity.getId(), role, TEST_ROLES, action, currentStatus);
                requireStatus(entity.getId(), action, currentStatus, Set.of("in_development"));
                yield "in_testing";
            }
            case "complete" -> {
                requireRole(entity.getId(), role, Set.of("test", "product", "admin"), action, currentStatus);
                requireStatus(entity.getId(), action, currentStatus, Set.of("in_testing"));
                yield "done";
            }
            case "reject" -> {
                requireRole(entity.getId(), role, Set.of("product", "backend", "frontend", "test", "admin"), action, currentStatus);
                requireStatus(entity.getId(), action, currentStatus, Set.of("draft", "in_review", "published", "accepted", "in_development", "in_testing"));
                yield "rejected";
            }
            case "reopen" -> {
                requireRole(entity.getId(), role, PRODUCT_ROLES, action, currentStatus);
                requireStatus(entity.getId(), action, currentStatus, Set.of("rejected"));
                yield "draft";
            }
            default -> throw new RequirementHandoffInvalidTransitionException(entity.getId(), action, currentStatus);
        };
    }

    private void requireRole(UUID handoffId, String role, Set<String> allowedRoles, String action, String currentStatus) {
        if (!allowedRoles.contains(role)) {
            throw new RequirementHandoffInvalidTransitionException(handoffId, action, currentStatus);
        }
    }

    private void requireStatus(UUID handoffId, String action, String currentStatus, Set<String> allowedStatus) {
        if (!allowedStatus.contains(currentStatus)) {
            throw new RequirementHandoffInvalidTransitionException(handoffId, action, currentStatus);
        }
    }

    private void ensureTaskAccess(RequirementTaskEntity task) {
        String role = RequestContext.getRole();
        if ("admin".equals(role)) {
            return;
        }
        if (!role.equals(task.getRole())) {
            throw new RequirementTaskAccessDeniedException(task.getId());
        }
    }

    private void ensureTaskReadable(RequirementTaskEntity task) {
        String role = RequestContext.getRole();
        if ("admin".equals(role)) {
            return;
        }
        if (TASK_OVERVIEW_ROLES.contains(role)) {
            return;
        }
        if (role.equals(task.getRole())) {
            return;
        }
        if (RequestContext.getActor().equals(task.getAssignee())) {
            return;
        }
        throw new RequirementTaskAccessDeniedException(task.getId());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String normalizeTaskView(String view) {
        if (view == null || view.isBlank()) {
            return "pool";
        }
        String normalized = view.trim().toLowerCase();
        if (Set.of("pool", "mine", "all").contains(normalized)) {
            return normalized;
        }
        return "pool";
    }

    private String normalizeTaskRoleFilter(String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return null;
        }
        String normalized = roleFilter.trim().toLowerCase();
        if (TASK_DOMAIN_ROLES.contains(normalized)) {
            return normalized;
        }
        return null;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 10;
        }
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private ChatSessionResponse toSessionResponse(ChatSessionEntity entity) {
        return new ChatSessionResponse(
                entity.getId().toString(),
                entity.getProjectId().toString(),
                entity.getOwnerActor(),
                entity.getRole(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }

    private RequirementHandoffResponse toHandoffResponse(RequirementHandoffEntity entity) {
        return new RequirementHandoffResponse(
                entity.getId().toString(),
                entity.getProjectId().toString(),
                entity.getSourceSessionId().toString(),
                entity.getVersion(),
                entity.getTitle(),
                entity.getRequirementSummary(),
                entity.getAcceptanceCriteria(),
                entity.getImpactScope(),
                entity.getPriority(),
                entity.getTargetRole(),
                entity.getStatus(),
                entity.getPublishedBy(),
                entity.getPublishedAt() == null ? null : entity.getPublishedAt().toString(),
                entity.getAcceptedBy(),
                entity.getAcceptedAt() == null ? null : entity.getAcceptedAt().toString(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessageEntity entity) {
        return new ChatMessageResponse(
                entity.getId().toString(),
                entity.getSessionId().toString(),
                entity.getMessageRole(),
                entity.getContent(),
                entity.getCreatedBy(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString()
        );
    }

    private RequirementTaskResponse toTaskResponse(RequirementTaskEntity entity) {
        String titleZh = isBlank(entity.getTitleZh()) ? entity.getTitle() : entity.getTitleZh();
        String titleEn = isBlank(entity.getTitleEn()) ? entity.getTitle() : entity.getTitleEn();
        String descriptionZh = isBlank(entity.getDescriptionZh()) ? entity.getDescription() : entity.getDescriptionZh();
        String descriptionEn = isBlank(entity.getDescriptionEn()) ? entity.getDescription() : entity.getDescriptionEn();
        return new RequirementTaskResponse(
                entity.getId().toString(),
                entity.getHandoffId().toString(),
                entity.getProjectId().toString(),
                entity.getRole(),
                entity.getTitle(),
                titleZh,
                titleEn,
                entity.getDescription(),
                descriptionZh,
                descriptionEn,
                entity.getEstimateHours(),
                entity.getStatus(),
                entity.getAssignee(),
                entity.getSource(),
                entity.getSortOrder(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }
}
