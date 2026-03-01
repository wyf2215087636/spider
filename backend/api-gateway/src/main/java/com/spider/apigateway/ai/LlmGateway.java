package com.spider.apigateway.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class LlmGateway {
    private static final Logger log = LoggerFactory.getLogger(LlmGateway.class);
    private static final String DEFAULT_FALLBACK = "LLM gateway fallback: no provider response available.";
    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public LlmGateway(LlmProperties llmProperties, ObjectMapper objectMapper) {
        this.llmProperties = llmProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(3, llmProperties.getTimeoutSeconds())))
                .build();
    }

    public GenerationResult generate(String systemPrompt, String userPrompt) {
        if (!llmProperties.isEnabled() || llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            return new GenerationResult(
                    DEFAULT_FALLBACK,
                    llmProperties.getProvider(),
                    llmProperties.getModel(),
                    true
            );
        }

        try {
            String body = buildRequestBody(systemPrompt, userPrompt, false);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolveChatCompletionsUrl()))
                    .timeout(Duration.ofSeconds(Math.max(3, llmProperties.getTimeoutSeconds())))
                    .header("Authorization", "Bearer " + llmProperties.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                        "llm.request.failed status={} provider={} model={} body={}",
                        response.statusCode(),
                        llmProperties.getProvider(),
                        llmProperties.getModel(),
                        response.body()
                );
                return new GenerationResult(DEFAULT_FALLBACK, llmProperties.getProvider(), llmProperties.getModel(), true);
            }
            String content = parseAssistantContent(response.body());
            if (content == null || content.isBlank()) {
                return new GenerationResult(DEFAULT_FALLBACK, llmProperties.getProvider(), llmProperties.getModel(), true);
            }
            return new GenerationResult(content, llmProperties.getProvider(), llmProperties.getModel(), false);
        } catch (Exception ex) {
            log.warn(
                    "llm.request.exception provider={} model={} detail={}",
                    llmProperties.getProvider(),
                    llmProperties.getModel(),
                    ex.getMessage()
            );
            return new GenerationResult(DEFAULT_FALLBACK, llmProperties.getProvider(), llmProperties.getModel(), true);
        }
    }

    public GenerationResult generateStream(
            String systemPrompt,
            String userPrompt,
            Consumer<String> deltaConsumer
    ) {
        if (!llmProperties.isEnabled() || llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            if (deltaConsumer != null) {
                emitChunks(DEFAULT_FALLBACK, deltaConsumer);
            }
            return new GenerationResult(
                    DEFAULT_FALLBACK,
                    llmProperties.getProvider(),
                    llmProperties.getModel(),
                    true
            );
        }

        try {
            String body = buildRequestBody(systemPrompt, userPrompt, true);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolveChatCompletionsUrl()))
                    .timeout(Duration.ofSeconds(Math.max(3, llmProperties.getTimeoutSeconds())))
                    .header("Authorization", "Bearer " + llmProperties.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                        "llm.stream.failed status={} provider={} model={}",
                        response.statusCode(),
                        llmProperties.getProvider(),
                        llmProperties.getModel()
                );
                GenerationResult fallback = generate(systemPrompt, userPrompt);
                if (deltaConsumer != null) {
                    emitChunks(fallback.content(), deltaConsumer);
                }
                return fallback;
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isBlank() || !trimmed.startsWith("data:")) {
                        continue;
                    }
                    String payload = trimmed.substring("data:".length()).trim();
                    if ("[DONE]".equals(payload)) {
                        break;
                    }
                    List<String> deltas = parseDeltaContents(payload);
                    for (String delta : deltas) {
                        content.append(delta);
                        if (deltaConsumer != null) {
                            deltaConsumer.accept(delta);
                        }
                    }
                }
            }

            if (content.isEmpty()) {
                GenerationResult fallback = generate(systemPrompt, userPrompt);
                if (deltaConsumer != null) {
                    emitChunks(fallback.content(), deltaConsumer);
                }
                return fallback;
            }

            return new GenerationResult(
                    content.toString(),
                    llmProperties.getProvider(),
                    llmProperties.getModel(),
                    false
            );
        } catch (Exception ex) {
            log.warn(
                    "llm.stream.exception provider={} model={} detail={}",
                    llmProperties.getProvider(),
                    llmProperties.getModel(),
                    ex.getMessage()
            );
            GenerationResult fallback = generate(systemPrompt, userPrompt);
            if (deltaConsumer != null) {
                emitChunks(fallback.content(), deltaConsumer);
            }
            return fallback;
        }
    }

    private String buildRequestBody(String systemPrompt, String userPrompt, boolean stream) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", llmProperties.getModel());
        payload.put("temperature", llmProperties.getTemperature());
        if (stream) {
            payload.put("stream", true);
        }
        payload.put(
                "messages",
                List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
        return objectMapper.writeValueAsString(payload);
    }

    private String parseAssistantContent(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }
        return choices.get(0).path("message").path("content").asText();
    }

    private List<String> parseDeltaContents(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return List.of();
        }
        List<String> deltas = new ArrayList<>();
        for (JsonNode choice : choices) {
            JsonNode contentNode = choice.path("delta").path("content");
            if (contentNode.isMissingNode() || contentNode.isNull()) {
                continue;
            }
            String content = contentNode.asText();
            if (content != null && !content.isEmpty()) {
                deltas.add(content);
            }
        }
        return deltas;
    }

    private void emitChunks(String text, Consumer<String> deltaConsumer) {
        if (text == null || text.isBlank()) {
            return;
        }
        int chunkSize = 24;
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(text.length(), i + chunkSize);
            deltaConsumer.accept(text.substring(i, end));
        }
    }

    private String resolveChatCompletionsUrl() {
        String base = llmProperties.getBaseUrl() == null ? "" : llmProperties.getBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/chat/completions";
    }

    public record GenerationResult(
            String content,
            String provider,
            String model,
            boolean fallback
    ) {
    }
}
