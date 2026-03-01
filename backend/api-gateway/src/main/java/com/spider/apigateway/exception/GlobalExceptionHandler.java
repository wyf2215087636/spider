package com.spider.apigateway.exception;

import com.spider.common.api.ApiResponse;
import com.spider.common.request.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
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
                .body(ApiResponse.error("WORKSPACE_NOT_FOUND", message));
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
                .body(ApiResponse.error("INTERNAL_ERROR", message));
    }
}
