package com.spider.apigateway.doc;

import com.spider.apigateway.doc.dto.CreateProductDocRequest;
import com.spider.apigateway.doc.dto.CreateProductDocAiMessageRequest;
import com.spider.apigateway.doc.dto.ConfirmProductDocRevisionRequest;
import com.spider.apigateway.doc.dto.ProductDocDetailResponse;
import com.spider.apigateway.doc.dto.ProductDocAiGenerateResponse;
import com.spider.apigateway.doc.dto.ProductDocAiMessageResponse;
import com.spider.apigateway.doc.dto.ProductDocResponse;
import com.spider.apigateway.doc.dto.ProductDocRevisionResponse;
import com.spider.apigateway.doc.dto.ProductDocVersionResponse;
import com.spider.apigateway.doc.dto.PublishProductDocVersionRequest;
import com.spider.apigateway.doc.dto.RejectProductDocRevisionRequest;
import com.spider.apigateway.doc.dto.RollbackProductDocRequest;
import com.spider.apigateway.doc.dto.UpdateProductDocRequest;
import com.spider.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product-docs")
public class ProductDocController {
    private final ProductDocService productDocService;

    public ProductDocController(ProductDocService productDocService) {
        this.productDocService = productDocService;
    }

    @PostMapping
    public ApiResponse<ProductDocDetailResponse> create(@Valid @RequestBody CreateProductDocRequest request) {
        return ApiResponse.success(productDocService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ProductDocResponse>> list(@RequestParam(required = false) String projectId) {
        return ApiResponse.success(productDocService.list(projectId));
    }

    @GetMapping("/{docId}")
    public ApiResponse<ProductDocDetailResponse> get(@PathVariable UUID docId) {
        return ApiResponse.success(productDocService.get(docId));
    }

    @PutMapping("/{docId}")
    public ApiResponse<ProductDocDetailResponse> update(
            @PathVariable UUID docId,
            @Valid @RequestBody UpdateProductDocRequest request
    ) {
        return ApiResponse.success(productDocService.update(docId, request));
    }

    @PostMapping("/{docId}/publish-version")
    public ApiResponse<ProductDocDetailResponse> publishVersion(
            @PathVariable UUID docId,
            @Valid @RequestBody PublishProductDocVersionRequest request
    ) {
        return ApiResponse.success(productDocService.publishVersion(docId, request));
    }

    @GetMapping("/{docId}/versions")
    public ApiResponse<List<ProductDocVersionResponse>> listVersions(@PathVariable UUID docId) {
        return ApiResponse.success(productDocService.listVersions(docId));
    }

    @GetMapping("/{docId}/versions/{versionId}")
    public ApiResponse<ProductDocVersionResponse> getVersion(
            @PathVariable UUID docId,
            @PathVariable UUID versionId
    ) {
        return ApiResponse.success(productDocService.getVersion(docId, versionId));
    }

    @PostMapping("/{docId}/rollback")
    public ApiResponse<ProductDocDetailResponse> rollback(
            @PathVariable UUID docId,
            @Valid @RequestBody RollbackProductDocRequest request
    ) {
        return ApiResponse.success(productDocService.rollback(docId, request));
    }

    @GetMapping("/{docId}/ai-chat/messages")
    public ApiResponse<List<ProductDocAiMessageResponse>> listAiMessages(@PathVariable UUID docId) {
        return ApiResponse.success(productDocService.listAiMessages(docId));
    }

    @PostMapping("/{docId}/ai-chat/messages")
    public ApiResponse<ProductDocAiGenerateResponse> sendAiMessage(
            @PathVariable UUID docId,
            @Valid @RequestBody CreateProductDocAiMessageRequest request
    ) {
        return ApiResponse.success(productDocService.sendAiMessage(docId, request));
    }

    @PostMapping(value = "/{docId}/ai-chat/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendAiMessageStream(
            @PathVariable UUID docId,
            @Valid @RequestBody CreateProductDocAiMessageRequest request
    ) {
        return productDocService.streamAiMessage(docId, request);
    }

    @GetMapping("/{docId}/ai-revisions")
    public ApiResponse<List<ProductDocRevisionResponse>> listAiRevisions(
            @PathVariable UUID docId,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(productDocService.listAiRevisions(docId, status));
    }

    @PostMapping("/{docId}/ai-revisions/{revisionId}/confirm")
    public ApiResponse<ProductDocDetailResponse> confirmAiRevision(
            @PathVariable UUID docId,
            @PathVariable UUID revisionId,
            @Valid @RequestBody(required = false) ConfirmProductDocRevisionRequest request
    ) {
        return ApiResponse.success(productDocService.confirmAiRevision(docId, revisionId, request));
    }

    @PostMapping("/{docId}/ai-revisions/{revisionId}/reject")
    public ApiResponse<ProductDocRevisionResponse> rejectAiRevision(
            @PathVariable UUID docId,
            @PathVariable UUID revisionId,
            @Valid @RequestBody(required = false) RejectProductDocRevisionRequest request
    ) {
        return ApiResponse.success(productDocService.rejectAiRevision(docId, revisionId, request));
    }
}
