package com.spider.apigateway.doc;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.apigateway.ai.LlmGateway;
import com.spider.apigateway.audit.AuditLogService;
import com.spider.apigateway.doc.dto.ConfirmProductDocRevisionRequest;
import com.spider.apigateway.doc.dto.CreateProductDocAiMessageRequest;
import com.spider.apigateway.doc.dto.CreateProductDocRequest;
import com.spider.apigateway.doc.dto.ProductDocAiGenerateResponse;
import com.spider.apigateway.doc.dto.ProductDocAiMessageResponse;
import com.spider.apigateway.doc.dto.ProductDocDetailResponse;
import com.spider.apigateway.doc.dto.ProductDocResponse;
import com.spider.apigateway.doc.dto.ProductDocRevisionResponse;
import com.spider.apigateway.doc.dto.ProductDocVersionResponse;
import com.spider.apigateway.doc.dto.PublishProductDocVersionRequest;
import com.spider.apigateway.doc.dto.RejectProductDocRevisionRequest;
import com.spider.apigateway.doc.dto.RollbackProductDocRequest;
import com.spider.apigateway.doc.dto.UpdateProductDocRequest;
import com.spider.apigateway.exception.AuthUnauthorizedException;
import com.spider.apigateway.exception.ProductDocAccessDeniedException;
import com.spider.apigateway.exception.ProductDocInvalidRollbackVersionException;
import com.spider.apigateway.exception.ProductDocNotFoundException;
import com.spider.apigateway.exception.ProductDocRevisionInvalidStateException;
import com.spider.apigateway.exception.ProductDocRevisionNotFoundException;
import com.spider.apigateway.exception.ProductDocVersionNotFoundException;
import com.spider.apigateway.exception.ProjectNotFoundException;
import com.spider.apigateway.project.ProjectEntity;
import com.spider.apigateway.project.ProjectMapper;
import com.spider.common.request.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProductDocService {
    private static final Logger log = LoggerFactory.getLogger(ProductDocService.class);
    private static final Set<String> DOC_WRITE_ROLES = Set.of("product", "admin");
    private static final Set<String> REVISION_STATUSES = Set.of("pending", "confirmed", "rejected");
    private static final int MAX_CHAT_CONTEXT = 16;

    private final ProductDocMapper productDocMapper;
    private final ProductDocVersionMapper productDocVersionMapper;
    private final ProductDocRevisionMapper productDocRevisionMapper;
    private final ProductDocChatMessageMapper productDocChatMessageMapper;
    private final ProjectMapper projectMapper;
    private final AuditLogService auditLogService;
    private final LlmGateway llmGateway;
    private final ObjectMapper objectMapper;

    public ProductDocService(
            ProductDocMapper productDocMapper,
            ProductDocVersionMapper productDocVersionMapper,
            ProductDocRevisionMapper productDocRevisionMapper,
            ProductDocChatMessageMapper productDocChatMessageMapper,
            ProjectMapper projectMapper,
            AuditLogService auditLogService,
            LlmGateway llmGateway,
            ObjectMapper objectMapper
    ) {
        this.productDocMapper = productDocMapper;
        this.productDocVersionMapper = productDocVersionMapper;
        this.productDocRevisionMapper = productDocRevisionMapper;
        this.productDocChatMessageMapper = productDocChatMessageMapper;
        this.projectMapper = projectMapper;
        this.auditLogService = auditLogService;
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper;
    }

    public ProductDocDetailResponse create(CreateProductDocRequest request) {
        ensureCreateRole();
        UUID projectId = UUID.fromString(request.getProjectId().trim());
        ensureProjectExists(projectId);

        ProductDocEntity entity = new ProductDocEntity();
        entity.setId(UUID.randomUUID());
        entity.setProjectId(projectId);
        entity.setTitle(request.getTitle().trim());
        entity.setStatus("draft");
        entity.setOwnerActor(RequestContext.getActor());
        entity.setDraftContent(request.getInitialContent() == null ? "" : request.getInitialContent().trim());
        entity.setCreatedAt(nowUtc());
        entity.setUpdatedAt(nowUtc());
        productDocMapper.insert(entity);

        appendAiMessage(entity.getId(), "system", "Document created. You can request AI revisions in this thread.", "system");

        auditLogService.recordSuccess(
                "product.doc.create",
                "product_doc",
                entity.getId().toString(),
                "title=" + entity.getTitle()
        );
        log.info("product.doc.create success docId={} projectId={} owner={}", entity.getId(), projectId, entity.getOwnerActor());
        return toDetailResponse(entity, null);
    }

    public List<ProductDocResponse> list(String projectId) {
        QueryWrapper<ProductDocEntity> query = new QueryWrapper<>();
        if (projectId != null && !projectId.isBlank()) {
            query.eq("project_id", UUID.fromString(projectId.trim()));
        }
        query.orderByDesc("updated_at");
        List<ProductDocEntity> docs = productDocMapper.selectList(query);
        Map<UUID, Integer> currentVersionNoMap = loadCurrentVersionNoMap(docs);
        return docs.stream()
                .map(doc -> {
                    UUID currentVersionId = doc.getCurrentVersionId();
                    Integer currentVersionNo = currentVersionId == null ? null : currentVersionNoMap.get(currentVersionId);
                    return toResponse(doc, currentVersionNo);
                })
                .toList();
    }

    public ProductDocDetailResponse get(UUID docId) {
        ProductDocEntity doc = getById(docId);
        ProductDocVersionEntity currentVersion = getCurrentVersion(doc);
        return toDetailResponse(doc, currentVersion);
    }

    public ProductDocDetailResponse update(UUID docId, UpdateProductDocRequest request) {
        ProductDocEntity doc = getById(docId);
        ensureWriteAccess(doc);

        doc.setTitle(request.getTitle().trim());
        doc.setDraftContent(request.getDraftContent().trim());
        doc.setStatus(request.getStatus().trim());
        doc.setUpdatedAt(nowUtc());
        productDocMapper.updateById(doc);

        auditLogService.recordSuccess(
                "product.doc.update",
                "product_doc",
                docId.toString(),
                "status=" + doc.getStatus()
        );
        log.info("product.doc.update success docId={} actor={}", docId, RequestContext.getActor());
        ProductDocVersionEntity currentVersion = getCurrentVersion(doc);
        return toDetailResponse(doc, currentVersion);
    }

    public ProductDocDetailResponse publishVersion(UUID docId, PublishProductDocVersionRequest request) {
        ProductDocEntity doc = getById(docId);
        ensureWriteAccess(doc);

        ProductDocVersionEntity version = new ProductDocVersionEntity();
        version.setId(UUID.randomUUID());
        version.setDocId(docId);
        version.setVersionNo(nextVersionNo(docId));
        version.setParentVersionId(doc.getCurrentVersionId());
        version.setContent(defaultText(doc.getDraftContent()));
        version.setChangeSummary(normalizeSummary(request.getChangeSummary(), "publish version " + version.getVersionNo()));
        version.setSourceType(normalizeSourceType(request.getSourceType()));
        version.setCreatedBy(RequestContext.getActor());
        version.setCreatedAt(nowUtc());
        productDocVersionMapper.insert(version);

        doc.setCurrentVersionId(version.getId());
        doc.setStatus("active");
        doc.setUpdatedAt(nowUtc());
        productDocMapper.updateById(doc);

        auditLogService.recordSuccess(
                "product.doc.publishVersion",
                "product_doc",
                docId.toString(),
                "version=" + version.getVersionNo() + ";sourceType=" + version.getSourceType()
        );
        log.info("product.doc.publishVersion success docId={} versionNo={} actor={}", docId, version.getVersionNo(), RequestContext.getActor());
        return toDetailResponse(doc, version);
    }

    public List<ProductDocVersionResponse> listVersions(UUID docId) {
        ProductDocEntity doc = getById(docId);
        QueryWrapper<ProductDocVersionEntity> query = new QueryWrapper<>();
        query.eq("doc_id", doc.getId());
        query.orderByDesc("version_no");
        return productDocVersionMapper.selectList(query).stream().map(this::toVersionResponse).toList();
    }

    public ProductDocVersionResponse getVersion(UUID docId, UUID versionId) {
        getById(docId);
        ProductDocVersionEntity version = getVersionById(versionId);
        if (!docId.equals(version.getDocId())) {
            throw new ProductDocVersionNotFoundException(versionId);
        }
        return toVersionResponse(version);
    }

    public ProductDocDetailResponse rollback(UUID docId, RollbackProductDocRequest request) {
        ProductDocEntity doc = getById(docId);
        ensureWriteAccess(doc);

        UUID targetVersionId = UUID.fromString(request.getTargetVersionId().trim());
        ProductDocVersionEntity targetVersion = getVersionById(targetVersionId);
        if (!docId.equals(targetVersion.getDocId())) {
            throw new ProductDocInvalidRollbackVersionException(docId, targetVersionId);
        }

        ProductDocVersionEntity rollbackVersion = new ProductDocVersionEntity();
        rollbackVersion.setId(UUID.randomUUID());
        rollbackVersion.setDocId(docId);
        rollbackVersion.setVersionNo(nextVersionNo(docId));
        rollbackVersion.setParentVersionId(doc.getCurrentVersionId());
        rollbackVersion.setContent(targetVersion.getContent());
        rollbackVersion.setChangeSummary(
                normalizeSummary(
                        request.getChangeSummary(),
                        "rollback to v" + targetVersion.getVersionNo()
                )
        );
        rollbackVersion.setSourceType("rollback");
        rollbackVersion.setCreatedBy(RequestContext.getActor());
        rollbackVersion.setCreatedAt(nowUtc());
        productDocVersionMapper.insert(rollbackVersion);

        doc.setDraftContent(targetVersion.getContent());
        doc.setCurrentVersionId(rollbackVersion.getId());
        doc.setStatus("active");
        doc.setUpdatedAt(nowUtc());
        productDocMapper.updateById(doc);

        auditLogService.recordSuccess(
                "product.doc.rollback",
                "product_doc",
                docId.toString(),
                "fromVersion=" + targetVersion.getVersionNo() + ";toVersion=" + rollbackVersion.getVersionNo()
        );
        log.info("product.doc.rollback success docId={} targetVersion={} newVersion={}", docId, targetVersion.getVersionNo(), rollbackVersion.getVersionNo());
        return toDetailResponse(doc, rollbackVersion);
    }

    public List<ProductDocAiMessageResponse> listAiMessages(UUID docId) {
        getById(docId);
        QueryWrapper<ProductDocChatMessageEntity> query = new QueryWrapper<>();
        query.eq("doc_id", docId);
        query.orderByAsc("created_at");
        return productDocChatMessageMapper.selectList(query).stream().map(this::toAiMessageResponse).toList();
    }

    public ProductDocAiGenerateResponse sendAiMessage(UUID docId, CreateProductDocAiMessageRequest request) {
        ProductDocEntity doc = getById(docId);
        ensureWriteAccess(doc);

        String instruction = request.getContent().trim();
        appendAiMessage(docId, "user", instruction, RequestContext.getActor());

        List<ProductDocChatMessageEntity> contextMessages = listLatestAiMessages(docId, MAX_CHAT_CONTEXT);
        LlmGateway.GenerationResult generation = llmGateway.generate(
                buildDocAiSystemPrompt(),
                buildDocAiUserPrompt(doc, instruction, contextMessages)
        );
        AiRevisionPayload payload = parseAiRevisionPayload(
                generation.content(),
                defaultText(doc.getDraftContent()),
                instruction
        );

        ProductDocChatMessageEntity assistant = appendAiMessage(docId, "assistant", payload.aiReply(), "ai-agent");

        ProductDocRevisionEntity revision = new ProductDocRevisionEntity();
        revision.setId(UUID.randomUUID());
        revision.setDocId(docId);
        revision.setSourceVersionId(doc.getCurrentVersionId());
        revision.setBaseContent(defaultText(doc.getDraftContent()));
        revision.setCandidateContent(payload.candidateContent());
        revision.setInstruction(instruction);
        revision.setChangeSummary(payload.changeSummary());
        revision.setStatus("pending");
        revision.setModelProvider(generation.provider());
        revision.setModelName(generation.model());
        revision.setCreatedBy(RequestContext.getActor());
        revision.setCreatedAt(nowUtc());
        revision.setUpdatedAt(nowUtc());
        productDocRevisionMapper.insert(revision);

        auditLogService.recordSuccess(
                "product.doc.aiRevisionGenerate",
                "product_doc",
                docId.toString(),
                "revisionId=" + revision.getId()
        );
        log.info(
                "product.doc.aiRevisionGenerate success docId={} revisionId={} actor={} provider={} model={}",
                docId,
                revision.getId(),
                RequestContext.getActor(),
                generation.provider(),
                generation.model()
        );
        return new ProductDocAiGenerateResponse(toAiMessageResponse(assistant), toRevisionResponse(revision));
    }

    public SseEmitter streamAiMessage(UUID docId, CreateProductDocAiMessageRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        String requestId = RequestContext.getRequestId();
        String actor = RequestContext.getActor();
        String role = RequestContext.getRole();
        CompletableFuture.runAsync(() -> {
            RequestContext.setRequestId(requestId);
            RequestContext.setActor(actor);
            RequestContext.setRole(role);
            try {
                ProductDocEntity doc = getById(docId);
                ensureWriteAccess(doc);
                String instruction = request.getContent().trim();
                appendAiMessage(docId, "user", instruction, RequestContext.getActor());

                List<ProductDocChatMessageEntity> contextMessages = listLatestAiMessages(docId, MAX_CHAT_CONTEXT);
                LlmGateway.GenerationResult generation = llmGateway.generateStream(
                        buildDocAiStreamSystemPrompt(),
                        buildDocAiUserPrompt(doc, instruction, contextMessages),
                        delta -> sendSseEvent(emitter, "delta", delta)
                );

                String candidateContent = sanitizeStreamedCandidate(
                        generation.content(),
                        defaultText(doc.getDraftContent())
                );
                String aiReply = cutText(candidateContent, 40000);
                ProductDocChatMessageEntity assistant = appendAiMessage(docId, "assistant", aiReply, "ai-agent");

                ProductDocRevisionEntity revision = new ProductDocRevisionEntity();
                revision.setId(UUID.randomUUID());
                revision.setDocId(docId);
                revision.setSourceVersionId(doc.getCurrentVersionId());
                revision.setBaseContent(defaultText(doc.getDraftContent()));
                revision.setCandidateContent(candidateContent);
                revision.setInstruction(instruction);
                revision.setChangeSummary(fallbackSummary(instruction));
                revision.setStatus("pending");
                revision.setModelProvider(generation.provider());
                revision.setModelName(generation.model());
                revision.setCreatedBy(RequestContext.getActor());
                revision.setCreatedAt(nowUtc());
                revision.setUpdatedAt(nowUtc());
                productDocRevisionMapper.insert(revision);

                doc.setDraftContent(candidateContent);
                doc.setUpdatedAt(nowUtc());
                productDocMapper.updateById(doc);

                Map<String, String> donePayload = new LinkedHashMap<>();
                donePayload.put("assistantMessageId", assistant.getId().toString());
                donePayload.put("revisionId", revision.getId().toString());
                donePayload.put("docId", doc.getId().toString());
                donePayload.put("status", "applied");
                sendSseEvent(emitter, "done", objectMapper.writeValueAsString(donePayload));
                emitter.complete();
            } catch (Exception ex) {
                String detail = ex.getMessage();
                if (detail == null || detail.isBlank()) {
                    detail = ex.getClass().getSimpleName();
                }
                log.error("product.doc.aiRevisionStream.failed docId={} requestId={} actor={} role={} detail={}",
                        docId, requestId, actor, role, detail, ex);
                try {
                    Map<String, String> errorPayload = new LinkedHashMap<>();
                    errorPayload.put("code", "STREAM_FAILED");
                    errorPayload.put("detail", detail);
                    errorPayload.put("requestId", defaultText(requestId));
                    sendSseEvent(emitter, "error", objectMapper.writeValueAsString(errorPayload));
                } catch (Exception jsonEx) {
                    sendSseEvent(emitter, "error", detail);
                }
                emitter.complete();
            } finally {
                RequestContext.clear();
            }
        });
        return emitter;
    }

    public List<ProductDocRevisionResponse> listAiRevisions(UUID docId, String status) {
        getById(docId);
        QueryWrapper<ProductDocRevisionEntity> query = new QueryWrapper<>();
        query.eq("doc_id", docId);
        String normalizedStatus = normalizeRevisionStatus(status);
        if (normalizedStatus != null) {
            query.eq("status", normalizedStatus);
        }
        query.orderByDesc("created_at");
        return productDocRevisionMapper.selectList(query).stream().map(this::toRevisionResponse).toList();
    }

    public ProductDocDetailResponse confirmAiRevision(
            UUID docId,
            UUID revisionId,
            ConfirmProductDocRevisionRequest request
    ) {
        ProductDocEntity doc = getById(docId);
        ensureWriteAccess(doc);

        ProductDocRevisionEntity revision = getRevisionById(revisionId);
        ensureRevisionBelongsDoc(docId, revision);
        ensureRevisionPending(revision);

        ProductDocVersionEntity version = new ProductDocVersionEntity();
        version.setId(UUID.randomUUID());
        version.setDocId(docId);
        version.setVersionNo(nextVersionNo(docId));
        version.setParentVersionId(doc.getCurrentVersionId());
        version.setContent(revision.getCandidateContent());
        String fallbackSummary = isBlank(revision.getChangeSummary())
                ? "confirm ai revision " + revision.getId()
                : revision.getChangeSummary();
        String requestSummary = request == null ? null : request.getChangeSummary();
        version.setChangeSummary(normalizeSummary(requestSummary, fallbackSummary));
        version.setSourceType("ai_confirm");
        version.setCreatedBy(RequestContext.getActor());
        version.setCreatedAt(nowUtc());
        productDocVersionMapper.insert(version);

        doc.setDraftContent(revision.getCandidateContent());
        doc.setCurrentVersionId(version.getId());
        doc.setStatus("active");
        doc.setUpdatedAt(nowUtc());
        productDocMapper.updateById(doc);

        revision.setStatus("confirmed");
        revision.setConfirmedBy(RequestContext.getActor());
        revision.setConfirmedAt(nowUtc());
        revision.setUpdatedAt(nowUtc());
        productDocRevisionMapper.updateById(revision);

        auditLogService.recordSuccess(
                "product.doc.aiRevisionConfirm",
                "product_doc",
                docId.toString(),
                "revisionId=" + revisionId + ";versionNo=" + version.getVersionNo()
        );
        log.info(
                "product.doc.aiRevisionConfirm success docId={} revisionId={} versionNo={} actor={}",
                docId,
                revisionId,
                version.getVersionNo(),
                RequestContext.getActor()
        );
        return toDetailResponse(doc, version);
    }

    public ProductDocRevisionResponse rejectAiRevision(
            UUID docId,
            UUID revisionId,
            RejectProductDocRevisionRequest request
    ) {
        ProductDocEntity doc = getById(docId);
        ensureWriteAccess(doc);

        ProductDocRevisionEntity revision = getRevisionById(revisionId);
        ensureRevisionBelongsDoc(docId, revision);
        ensureRevisionPending(revision);

        revision.setStatus("rejected");
        revision.setConfirmedBy(RequestContext.getActor());
        revision.setConfirmedAt(nowUtc());
        revision.setUpdatedAt(nowUtc());
        productDocRevisionMapper.updateById(revision);

        String reason = request == null ? "" : defaultText(request.getReason()).trim();
        String rejectNote = reason.isBlank()
                ? "Revision rejected by " + RequestContext.getActor()
                : "Revision rejected: " + reason;
        appendAiMessage(doc.getId(), "system", rejectNote, RequestContext.getActor());

        auditLogService.recordSuccess(
                "product.doc.aiRevisionReject",
                "product_doc",
                docId.toString(),
                "revisionId=" + revisionId
        );
        log.info(
                "product.doc.aiRevisionReject success docId={} revisionId={} actor={}",
                docId,
                revisionId,
                RequestContext.getActor()
        );
        return toRevisionResponse(revision);
    }

    private ProductDocEntity getById(UUID docId) {
        ProductDocEntity entity = productDocMapper.selectById(docId);
        if (entity == null) {
            throw new ProductDocNotFoundException(docId);
        }
        return entity;
    }

    private ProductDocVersionEntity getVersionById(UUID versionId) {
        ProductDocVersionEntity entity = productDocVersionMapper.selectById(versionId);
        if (entity == null) {
            throw new ProductDocVersionNotFoundException(versionId);
        }
        return entity;
    }

    private ProductDocRevisionEntity getRevisionById(UUID revisionId) {
        ProductDocRevisionEntity entity = productDocRevisionMapper.selectById(revisionId);
        if (entity == null) {
            throw new ProductDocRevisionNotFoundException(revisionId);
        }
        return entity;
    }

    private ProductDocVersionEntity getCurrentVersion(ProductDocEntity doc) {
        if (doc.getCurrentVersionId() == null) {
            return null;
        }
        return productDocVersionMapper.selectById(doc.getCurrentVersionId());
    }

    private int nextVersionNo(UUID docId) {
        QueryWrapper<ProductDocVersionEntity> query = new QueryWrapper<>();
        query.eq("doc_id", docId);
        query.orderByDesc("version_no");
        query.last("LIMIT 1");
        ProductDocVersionEntity latest = productDocVersionMapper.selectOne(query);
        return latest == null ? 1 : latest.getVersionNo() + 1;
    }

    private void ensureCreateRole() {
        if (!DOC_WRITE_ROLES.contains(RequestContext.getRole())) {
            throw new AuthUnauthorizedException();
        }
    }

    private void ensureWriteAccess(ProductDocEntity doc) {
        String role = RequestContext.getRole();
        if ("admin".equals(role)) {
            return;
        }
        if (!"product".equals(role) || !RequestContext.getActor().equals(doc.getOwnerActor())) {
            throw new ProductDocAccessDeniedException(doc.getId());
        }
    }

    private void ensureProjectExists(UUID projectId) {
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ProjectNotFoundException(projectId);
        }
    }

    private void ensureRevisionBelongsDoc(UUID docId, ProductDocRevisionEntity revision) {
        if (!docId.equals(revision.getDocId())) {
            throw new ProductDocRevisionNotFoundException(revision.getId());
        }
    }

    private void ensureRevisionPending(ProductDocRevisionEntity revision) {
        if (!"pending".equals(revision.getStatus())) {
            throw new ProductDocRevisionInvalidStateException(revision.getId(), revision.getStatus());
        }
    }

    private Map<UUID, Integer> loadCurrentVersionNoMap(List<ProductDocEntity> docs) {
        List<UUID> versionIds = docs.stream()
                .map(ProductDocEntity::getCurrentVersionId)
                .filter(id -> id != null)
                .toList();
        if (versionIds.isEmpty()) {
            return Map.of();
        }
        return productDocVersionMapper.selectBatchIds(versionIds).stream()
                .collect(Collectors.toMap(ProductDocVersionEntity::getId, ProductDocVersionEntity::getVersionNo));
    }

    private String normalizeSummary(String input, String fallback) {
        if (input == null || input.isBlank()) {
            return cutText(fallback, 500);
        }
        return cutText(input.trim(), 500);
    }

    private String normalizeSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return "manual";
        }
        String value = sourceType.trim();
        return switch (value) {
            case "ai_confirm" -> "ai_confirm";
            default -> "manual";
        };
    }

    private String normalizeRevisionStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim();
        return REVISION_STATUSES.contains(normalized) ? normalized : null;
    }

    private ProductDocChatMessageEntity appendAiMessage(UUID docId, String role, String content, String createdBy) {
        ProductDocChatMessageEntity message = new ProductDocChatMessageEntity();
        message.setId(UUID.randomUUID());
        message.setDocId(docId);
        message.setMessageRole(role);
        message.setContent(defaultText(content));
        message.setCreatedBy(defaultText(createdBy));
        message.setCreatedAt(nowUtc());
        productDocChatMessageMapper.insert(message);
        return message;
    }

    private List<ProductDocChatMessageEntity> listLatestAiMessages(UUID docId, int limit) {
        QueryWrapper<ProductDocChatMessageEntity> query = new QueryWrapper<>();
        query.eq("doc_id", docId);
        query.orderByDesc("created_at");
        query.last("LIMIT " + Math.max(1, limit));
        List<ProductDocChatMessageEntity> rows = productDocChatMessageMapper.selectList(query);
        return rows.stream().sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())).toList();
    }

    private String buildDocAiSystemPrompt() {
        return """
                You are a product document revision assistant.
                You should revise the CURRENT draft, not create an independent new proposal.
                Return STRICT JSON only, with no markdown.
                Output schema:
                {
                  \"aiReply\": \"short response to the user\",
                  \"changeSummary\": \"summary in one sentence\",
                  \"candidateContent\": \"full rewritten product document content\"
                }
                candidateContent must be the full revised document body.
                """;
    }

    private String buildDocAiStreamSystemPrompt() {
        return """
                You are a product document rewriting assistant for product managers.
                You must rewrite the CURRENT draft based on instruction and context.
                Return ONLY the final revised markdown document body.
                Do not output JSON, code fences, or explanations.
                """;
    }

    private String buildDocAiUserPrompt(
            ProductDocEntity doc,
            String instruction,
            List<ProductDocChatMessageEntity> contextMessages
    ) {
        String context = contextMessages.stream()
                .map(item -> "[" + item.getMessageRole() + "] " + defaultText(item.getContent()))
                .collect(Collectors.joining("\n"));
        return """
                Document title: %s
                Document owner: %s
                Current draft:
                %s

                Latest chat context:
                %s

                New instruction:
                %s
                """.formatted(
                doc.getTitle(),
                doc.getOwnerActor(),
                defaultText(doc.getDraftContent()),
                context,
                instruction
        );
    }

    private AiRevisionPayload parseAiRevisionPayload(String rawContent, String baseContent, String instruction) {
        if (rawContent != null && !rawContent.isBlank()) {
            try {
                String json = stripCodeFence(rawContent.trim());
                JsonNode root = objectMapper.readTree(json);
                String aiReply = readText(root, "aiReply", "AI revision draft generated.");
                String changeSummary = normalizeSummary(readText(root, "changeSummary", fallbackSummary(instruction)), fallbackSummary(instruction));
                String candidateContent = readText(root, "candidateContent", "");
                if (candidateContent.isBlank()) {
                    candidateContent = fallbackCandidate(baseContent, instruction);
                }
                return new AiRevisionPayload(aiReply, candidateContent, changeSummary);
            } catch (Exception ex) {
                log.warn("product.doc.aiRevision.parseFailed detail={}", ex.getMessage());
                String candidateContent = stripCodeFence(rawContent.trim());
                if (!candidateContent.isBlank()) {
                    return new AiRevisionPayload(
                            "AI revision draft generated from current instruction.",
                            candidateContent,
                            fallbackSummary(instruction)
                    );
                }
            }
        }

        return new AiRevisionPayload(
                "AI revision draft generated from current instruction.",
                fallbackCandidate(baseContent, instruction),
                fallbackSummary(instruction)
        );
    }

    private String readText(JsonNode root, String key, String fallback) {
        JsonNode node = root.path(key);
        if (node.isMissingNode() || node.isNull()) {
            return fallback;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String stripCodeFence(String input) {
        String text = input;
        if (text.startsWith("```")) {
            int firstLine = text.indexOf('\n');
            if (firstLine > 0) {
                text = text.substring(firstLine + 1);
            }
            int endFence = text.lastIndexOf("```");
            if (endFence >= 0) {
                text = text.substring(0, endFence);
            }
        }
        return text.trim();
    }

    private String fallbackSummary(String instruction) {
        if (instruction == null || instruction.isBlank()) {
            return "AI revision";
        }
        return cutText("AI revision: " + instruction.trim(), 500);
    }

    private String fallbackCandidate(String baseContent, String instruction) {
        String base = defaultText(baseContent).trim();
        String ask = defaultText(instruction).trim();
        if (base.isBlank() && ask.isBlank()) {
            return "";
        }
        if (base.isBlank()) {
            return ask;
        }
        if (ask.isBlank()) {
            return base;
        }
        return base;
    }

    private String sanitizeStreamedCandidate(String streamedContent, String fallbackContent) {
        String cleaned = stripCodeFence(defaultText(streamedContent).trim());
        if (!cleaned.isBlank()) {
            return cleaned;
        }
        return defaultText(fallbackContent);
    }

    private void sendSseEvent(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name(event)
                            .data(data == null ? "" : data)
            );
        } catch (Exception ignored) {
            // client may close connection; service continues best effort
        }
    }

    private OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private String cutText(String text, int maxLength) {
        String safe = defaultText(text);
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, maxLength);
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private ProductDocResponse toResponse(ProductDocEntity entity, Integer currentVersionNo) {
        return new ProductDocResponse(
                entity.getId().toString(),
                entity.getProjectId().toString(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getOwnerActor(),
                entity.getCurrentVersionId() == null ? null : entity.getCurrentVersionId().toString(),
                currentVersionNo,
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }

    private ProductDocDetailResponse toDetailResponse(ProductDocEntity entity, ProductDocVersionEntity currentVersion) {
        Integer currentVersionNo = currentVersion == null ? null : currentVersion.getVersionNo();
        ProductDocVersionResponse currentVersionResponse = currentVersion == null ? null : toVersionResponse(currentVersion);
        return new ProductDocDetailResponse(
                entity.getId().toString(),
                entity.getProjectId().toString(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getOwnerActor(),
                entity.getDraftContent(),
                entity.getCurrentVersionId() == null ? null : entity.getCurrentVersionId().toString(),
                currentVersionNo,
                currentVersionResponse,
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }

    private ProductDocVersionResponse toVersionResponse(ProductDocVersionEntity entity) {
        return new ProductDocVersionResponse(
                entity.getId().toString(),
                entity.getDocId().toString(),
                entity.getVersionNo(),
                entity.getParentVersionId() == null ? null : entity.getParentVersionId().toString(),
                entity.getContent(),
                entity.getChangeSummary(),
                entity.getSourceType(),
                entity.getCreatedBy(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString()
        );
    }

    private ProductDocAiMessageResponse toAiMessageResponse(ProductDocChatMessageEntity entity) {
        return new ProductDocAiMessageResponse(
                entity.getId().toString(),
                entity.getDocId().toString(),
                entity.getMessageRole(),
                entity.getContent(),
                entity.getCreatedBy(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString()
        );
    }

    private ProductDocRevisionResponse toRevisionResponse(ProductDocRevisionEntity entity) {
        return new ProductDocRevisionResponse(
                entity.getId().toString(),
                entity.getDocId().toString(),
                entity.getSourceVersionId() == null ? null : entity.getSourceVersionId().toString(),
                entity.getBaseContent(),
                entity.getCandidateContent(),
                entity.getInstruction(),
                entity.getChangeSummary(),
                entity.getStatus(),
                entity.getModelProvider(),
                entity.getModelName(),
                entity.getCreatedBy(),
                entity.getConfirmedBy(),
                entity.getConfirmedAt() == null ? null : entity.getConfirmedAt().toString(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString()
        );
    }

    private record AiRevisionPayload(
            String aiReply,
            String candidateContent,
            String changeSummary
    ) {
    }
}
