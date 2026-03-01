package com.spider.apigateway.exception;

import com.spider.common.api.ApiResponse;
import com.spider.common.request.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleWorkspaceNotFound(
            WorkspaceNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "workspace.notFound",
                new Object[]{ex.getWorkspaceId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=WORKSPACE_NOT_FOUND workspaceId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getWorkspaceId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("WORKSPACE_NOT_FOUND", message));
    }

    @ExceptionHandler(AuthInvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthInvalidCredentials(
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage("auth.invalidCredentials", null, locale);
        log.warn(
                "requestId={} method={} path={} errorCode=AUTH_INVALID_CREDENTIALS",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("AUTH_INVALID_CREDENTIALS", message));
    }

    @ExceptionHandler(AuthUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthUnauthorized(
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage("auth.unauthorized", null, locale);
        log.warn(
                "requestId={} method={} path={} errorCode=UNAUTHORIZED",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("UNAUTHORIZED", message));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectNotFound(
            ProjectNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "project.notFound",
                new Object[]{ex.getProjectId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=PROJECT_NOT_FOUND projectId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getProjectId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("PROJECT_NOT_FOUND", message));
    }

    @ExceptionHandler(ProductDocNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductDocNotFound(
            ProductDocNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "productDoc.notFound",
                new Object[]{ex.getDocId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=PRODUCT_DOC_NOT_FOUND docId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getDocId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("PRODUCT_DOC_NOT_FOUND", message));
    }

    @ExceptionHandler(ProductDocVersionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductDocVersionNotFound(
            ProductDocVersionNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "productDoc.versionNotFound",
                new Object[]{ex.getVersionId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=PRODUCT_DOC_VERSION_NOT_FOUND versionId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getVersionId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("PRODUCT_DOC_VERSION_NOT_FOUND", message));
    }

    @ExceptionHandler(ProductDocAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductDocAccessDenied(
            ProductDocAccessDeniedException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "productDoc.accessDenied",
                new Object[]{ex.getDocId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=PRODUCT_DOC_ACCESS_DENIED docId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getDocId()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("PRODUCT_DOC_ACCESS_DENIED", message));
    }

    @ExceptionHandler(ProductDocInvalidRollbackVersionException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductDocInvalidRollbackVersion(
            ProductDocInvalidRollbackVersionException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "productDoc.invalidRollbackVersion",
                new Object[]{ex.getDocId(), ex.getVersionId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=PRODUCT_DOC_INVALID_ROLLBACK_VERSION docId={} versionId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getDocId(),
                ex.getVersionId()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("PRODUCT_DOC_INVALID_ROLLBACK_VERSION", message));
    }

    @ExceptionHandler(ProductDocRevisionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductDocRevisionNotFound(
            ProductDocRevisionNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "productDoc.revisionNotFound",
                new Object[]{ex.getRevisionId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=PRODUCT_DOC_REVISION_NOT_FOUND revisionId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getRevisionId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("PRODUCT_DOC_REVISION_NOT_FOUND", message));
    }

    @ExceptionHandler(ProductDocRevisionInvalidStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductDocRevisionInvalidState(
            ProductDocRevisionInvalidStateException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "productDoc.revisionInvalidState",
                new Object[]{ex.getRevisionId(), ex.getCurrentStatus()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=PRODUCT_DOC_REVISION_INVALID_STATE revisionId={} status={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getRevisionId(),
                ex.getCurrentStatus()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("PRODUCT_DOC_REVISION_INVALID_STATE", message));
    }

    @ExceptionHandler(ChatSessionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleChatSessionNotFound(
            ChatSessionNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "chatSession.notFound",
                new Object[]{ex.getChatSessionId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=CHAT_SESSION_NOT_FOUND chatSessionId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getChatSessionId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("CHAT_SESSION_NOT_FOUND", message));
    }

    @ExceptionHandler(ChatSessionAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleChatSessionAccessDenied(
            ChatSessionAccessDeniedException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "chatSession.accessDenied",
                new Object[]{ex.getChatSessionId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=CHAT_SESSION_ACCESS_DENIED chatSessionId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getChatSessionId()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("CHAT_SESSION_ACCESS_DENIED", message));
    }

    @ExceptionHandler(RequirementHandoffNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementHandoffNotFound(
            RequirementHandoffNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementHandoff.notFound",
                new Object[]{ex.getHandoffId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_HANDOFF_NOT_FOUND handoffId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getHandoffId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_HANDOFF_NOT_FOUND", message));
    }

    @ExceptionHandler(RequirementHandoffInvalidStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementHandoffInvalidState(
            RequirementHandoffInvalidStateException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementHandoff.invalidState",
                new Object[]{ex.getHandoffId(), ex.getCurrentStatus()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_HANDOFF_INVALID_STATE handoffId={} currentStatus={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getHandoffId(),
                ex.getCurrentStatus()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_HANDOFF_INVALID_STATE", message));
    }

    @ExceptionHandler(RequirementHandoffInvalidTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementHandoffInvalidTransition(
            RequirementHandoffInvalidTransitionException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementHandoff.invalidTransition",
                new Object[]{ex.getHandoffId(), ex.getAction(), ex.getCurrentStatus()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_HANDOFF_INVALID_TRANSITION handoffId={} action={} currentStatus={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getHandoffId(),
                ex.getAction(),
                ex.getCurrentStatus()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_HANDOFF_INVALID_TRANSITION", message));
    }

    @ExceptionHandler(RequirementHandoffInsufficientContentException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementHandoffInsufficientContent(
            RequirementHandoffInsufficientContentException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementHandoff.insufficientContent",
                new Object[]{ex.getHandoffId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_HANDOFF_INSUFFICIENT_CONTENT handoffId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getHandoffId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_HANDOFF_INSUFFICIENT_CONTENT", message));
    }

    @ExceptionHandler(RequirementTaskNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementTaskNotFound(
            RequirementTaskNotFoundException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementTask.notFound",
                new Object[]{ex.getTaskId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_TASK_NOT_FOUND taskId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getTaskId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_TASK_NOT_FOUND", message));
    }

    @ExceptionHandler(RequirementTaskAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementTaskAccessDenied(
            RequirementTaskAccessDeniedException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementTask.accessDenied",
                new Object[]{ex.getTaskId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_TASK_ACCESS_DENIED taskId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getTaskId()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_TASK_ACCESS_DENIED", message));
    }

    @ExceptionHandler(RequirementTaskInvalidStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementTaskInvalidStatus(
            RequirementTaskInvalidStatusException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementTask.invalidStatus",
                new Object[]{ex.getTaskId(), ex.getStatus()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_TASK_INVALID_STATUS taskId={} status={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getTaskId(),
                ex.getStatus()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_TASK_INVALID_STATUS", message));
    }

    @ExceptionHandler(RequirementTaskPlanGenerationException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequirementTaskPlanGenerationFailed(
            RequirementTaskPlanGenerationException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "requirementTask.planGenerationFailed",
                new Object[]{ex.getHandoffId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=REQUIREMENT_TASK_PLAN_GENERATION_FAILED handoffId={} reason={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getHandoffId(),
                ex.getReason()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("REQUIREMENT_TASK_PLAN_GENERATION_FAILED", message));
    }

    @ExceptionHandler(WorkspaceHasProjectsException.class)
    public ResponseEntity<ApiResponse<Void>> handleWorkspaceHasProjects(
            WorkspaceHasProjectsException ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
                "workspace.hasProjects",
                new Object[]{ex.getWorkspaceId()},
                locale
        );
        log.warn(
                "requestId={} method={} path={} errorCode=WORKSPACE_HAS_PROJECTS workspaceId={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getWorkspaceId()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("WORKSPACE_HAS_PROJECTS", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        FieldError firstError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = firstError == null ? "Invalid request" : firstError.getDefaultMessage();
        log.warn(
                "requestId={} method={} path={} errorCode=VALIDATION_ERROR message={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                message
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage("internal.error", null, locale);
        log.error(
                "requestId={} method={} path={} errorCode=INTERNAL_ERROR detail={}",
                RequestContext.getRequestId(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error("INTERNAL_ERROR", message));
    }
}


