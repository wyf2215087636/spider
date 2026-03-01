package com.spider.apigateway.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.apigateway.handoff.ChatMessageEntity;
import com.spider.apigateway.handoff.ChatSessionEntity;
import com.spider.apigateway.handoff.RequirementHandoffEntity;
import com.spider.apigateway.handoff.dto.AiDraftResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AiDraftService {
    private static final List<String> DEFAULT_TASKS = List.of(
            "Clarify requirement boundary and non-goals with product role",
            "Update backend/frontend contract and migration plan",
            "Prepare test cases and regression checklist"
    );
    private static final List<String> SUPPORTED_TARGET_ROLES = List.of("product", "backend", "frontend", "test");
    private static final List<String> SUPPORTED_PRIORITIES = List.of("P0", "P1", "P2", "P3");

    private final LlmGateway llmGateway;
    private final CodeImpactAgent codeImpactAgent;
    private final ObjectMapper objectMapper;

    public AiDraftService(
            LlmGateway llmGateway,
            CodeImpactAgent codeImpactAgent,
            ObjectMapper objectMapper
    ) {
        this.llmGateway = llmGateway;
        this.codeImpactAgent = codeImpactAgent;
        this.objectMapper = objectMapper;
    }

    public AiDraftResponse generateDraft(
            ChatSessionEntity session,
            List<ChatMessageEntity> latestMessages,
            String requirementInput,
            String targetRole,
            String priority
    ) {
        String safeRole = normalizeTargetRole(targetRole);
        String safePriority = normalizePriority(priority);

        String sessionContext = buildSessionContext(latestMessages);
        String seed = (requirementInput == null || requirementInput.isBlank()) ? sessionContext : requirementInput.trim();
        CodeImpactAgent.CodeImpactReport impactReport = codeImpactAgent.analyze(seed, 8);

        String systemPrompt = """
                You are a software delivery planning assistant.
                You must return STRICT JSON only, with no markdown.
                JSON fields:
                title, requirementSummary, acceptanceCriteria, impactScope, priority, targetRole, suggestedTasks(array), aiReply.
                """;

        String userPrompt = """
                Project session title: %s
                Session role: %s
                Request input:
                %s

                Recent chat context:
                %s

                Code impact summary:
                %s
                Impacted files:
                %s

                Constraints:
                - targetRole must be one of [product, backend, frontend, test], default is %s
                - priority must be one of [P0, P1, P2, P3], default is %s
                - acceptanceCriteria should be concise bullet style in plain text
                """.formatted(
                session.getTitle(),
                session.getRole(),
                nullToEmpty(requirementInput),
                sessionContext,
                impactReport.summary(),
                String.join(", ", impactReport.impactedFiles()),
                safeRole,
                safePriority
        );

        LlmGateway.GenerationResult llmResult = llmGateway.generate(systemPrompt, userPrompt);
        DraftPayload payload = parsePayload(llmResult.content(), session.getTitle(), safeRole, safePriority, impactReport, seed);

        return new AiDraftResponse(
                payload.title(),
                payload.requirementSummary(),
                payload.acceptanceCriteria(),
                payload.impactScope(),
                payload.priority(),
                payload.targetRole(),
                payload.suggestedTasks(),
                impactReport.impactedFiles(),
                impactReport.riskHints(),
                impactReport.testHints(),
                payload.aiReply(),
                llmResult.provider(),
                llmResult.model()
        );
    }

    public TaskPlanResult generateTaskPlan(RequirementHandoffEntity handoff) {
        String seed = String.join(
                "\n",
                nullToEmpty(handoff.getTitle()),
                nullToEmpty(handoff.getRequirementSummary()),
                nullToEmpty(handoff.getAcceptanceCriteria()),
                nullToEmpty(handoff.getImpactScope())
        );
        CodeImpactAgent.CodeImpactReport impactReport = codeImpactAgent.analyze(seed, 10);

        String systemPrompt = """
                You are a software project task planner.
                Return STRICT JSON only, no markdown.
                JSON format:
                {
                  "rationale": "text",
                  "tasks": [
                    {
                      "role":"backend|frontend|test|product",
                      "titleZh":"...",
                      "titleEn":"...",
                      "descriptionZh":"...",
                      "descriptionEn":"...",
                      "estimateHours":4
                    }
                  ]
                }
                Ensure at least 3 tasks and include backend, frontend, test when relevant.
                """;
        String userPrompt = """
                Requirement title: %s
                Priority: %s
                Target role: %s
                Summary: %s
                Acceptance criteria: %s
                Impact scope: %s
                Code impact summary: %s
                Impacted files: %s
                """.formatted(
                nullToEmpty(handoff.getTitle()),
                nullToEmpty(handoff.getPriority()),
                nullToEmpty(handoff.getTargetRole()),
                nullToEmpty(handoff.getRequirementSummary()),
                nullToEmpty(handoff.getAcceptanceCriteria()),
                nullToEmpty(handoff.getImpactScope()),
                impactReport.summary(),
                String.join(", ", impactReport.impactedFiles())
        );

        LlmGateway.GenerationResult llmResult = llmGateway.generate(systemPrompt, userPrompt);
        List<TaskPlanItem> tasks = parseTaskPlan(llmResult.content());
        String rationale = tasks.isEmpty()
                ? "No structured task output parsed from LLM response."
                : "AI task plan generated from requirement package and code impact evidence.";
        return new TaskPlanResult(
                tasks,
                impactReport.impactedFiles(),
                impactReport.riskHints(),
                impactReport.testHints(),
                llmResult.provider(),
                llmResult.model(),
                rationale
        );
    }

    private DraftPayload parsePayload(
            String rawContent,
            String sessionTitle,
            String defaultRole,
            String defaultPriority,
            CodeImpactAgent.CodeImpactReport impactReport,
            String seed
    ) {
        if (rawContent != null && !rawContent.isBlank()) {
            try {
                String jsonText = stripCodeFence(rawContent.trim());
                JsonNode root = objectMapper.readTree(jsonText);
                String title = readText(root, "title", sessionTitle + " - AI Draft");
                String summary = readText(root, "requirementSummary", defaultSummary(seed));
                String criteria = readText(root, "acceptanceCriteria", defaultCriteria(impactReport));
                String scope = readText(root, "impactScope", impactReport.summary());
                String priority = normalizePriority(readText(root, "priority", defaultPriority));
                String targetRole = normalizeTargetRole(readText(root, "targetRole", defaultRole));
                List<String> tasks = readStringArray(root.path("suggestedTasks"));
                if (tasks.isEmpty()) {
                    tasks = DEFAULT_TASKS;
                }
                String aiReply = readText(root, "aiReply", "AI draft generated with code impact evidence.");
                return new DraftPayload(title, summary, criteria, scope, priority, targetRole, tasks, aiReply);
            } catch (Exception ignored) {
                // fall through to deterministic fallback
            }
        }

        return new DraftPayload(
                sessionTitle + " - AI Draft",
                defaultSummary(seed),
                defaultCriteria(impactReport),
                impactReport.summary(),
                defaultPriority,
                defaultRole,
                DEFAULT_TASKS,
                "AI fallback draft generated due unavailable structured LLM response."
        );
    }

    private String buildSessionContext(List<ChatMessageEntity> latestMessages) {
        if (latestMessages == null || latestMessages.isEmpty()) {
            return "";
        }
        return latestMessages.stream()
                .filter(Objects::nonNull)
                .map(item -> "[" + item.getMessageRole() + "] " + nullToEmpty(item.getContent()))
                .collect(Collectors.joining("\n"));
    }

    private String defaultSummary(String seed) {
        String source = nullToEmpty(seed);
        if (source.length() > 600) {
            source = source.substring(0, 600);
        }
        if (source.isBlank()) {
            return "Refine requirement details based on latest role discussion and publish executable delivery package.";
        }
        return source;
    }

    private String defaultCriteria(CodeImpactAgent.CodeImpactReport impactReport) {
        List<String> lines = new ArrayList<>();
        lines.add("1) Requirement package can be published and accepted through the handoff center.");
        lines.add("2) Impacted API and UI changes are synchronized without validation errors.");
        if (!impactReport.testHints().isEmpty()) {
            lines.add("3) " + impactReport.testHints().get(0));
        }
        return String.join("\n", lines);
    }

    private String normalizeTargetRole(String targetRole) {
        if (targetRole == null || targetRole.isBlank()) {
            return "backend";
        }
        String normalized = targetRole.trim().toLowerCase(Locale.ROOT);
        return SUPPORTED_TARGET_ROLES.contains(normalized) ? normalized : "backend";
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "P2";
        }
        String normalized = priority.trim().toUpperCase(Locale.ROOT);
        return SUPPORTED_PRIORITIES.contains(normalized) ? normalized : "P2";
    }

    private String readText(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.path(field);
        if (node.isMissingNode() || node.isNull()) {
            return defaultValue;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private List<String> readStringArray(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        node.forEach(item -> {
            String text = item.asText();
            if (text != null && !text.isBlank()) {
                result.add(text.trim());
            }
        });
        return result;
    }

    private List<TaskPlanItem> parseTaskPlan(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return List.of();
        }
        try {
            String jsonText = stripCodeFence(rawContent.trim());
            JsonNode root = objectMapper.readTree(jsonText);
            JsonNode tasksNode = root.path("tasks");
            if (!tasksNode.isArray()) {
                return List.of();
            }
            List<TaskPlanItem> tasks = new ArrayList<>();
            tasksNode.forEach(item -> {
                String role = normalizeTargetRole(readText(item, "role", "backend"));
                String titleZh = readText(item, "titleZh", "");
                String titleEn = readText(item, "titleEn", "");
                String descriptionZh = readText(item, "descriptionZh", "");
                String descriptionEn = readText(item, "descriptionEn", "");
                int estimateHours = item.path("estimateHours").asInt(4);
                if (estimateHours < 1) {
                    estimateHours = 1;
                }
                if (estimateHours > 200) {
                    estimateHours = 200;
                }
                if (titleZh.isBlank() && titleEn.isBlank()) {
                    return;
                }
                if (titleZh.isBlank()) {
                    titleZh = titleEn;
                }
                if (titleEn.isBlank()) {
                    titleEn = titleZh;
                }
                if (descriptionZh.isBlank() && descriptionEn.isBlank()) {
                    descriptionEn = "Follow requirement package to complete the scoped changes.";
                    descriptionZh = "根据需求包完成范围内改造并同步验证。";
                } else {
                    if (descriptionZh.isBlank()) {
                        descriptionZh = descriptionEn;
                    }
                    if (descriptionEn.isBlank()) {
                        descriptionEn = descriptionZh;
                    }
                }
                tasks.add(new TaskPlanItem(role, titleZh, titleEn, descriptionZh, descriptionEn, estimateHours));
            });
            return tasks;
        } catch (Exception ignored) {
            return List.of();
        }
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

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private record DraftPayload(
            String title,
            String requirementSummary,
            String acceptanceCriteria,
            String impactScope,
            String priority,
            String targetRole,
            List<String> suggestedTasks,
            String aiReply
    ) {
    }

    public record TaskPlanItem(
            String role,
            String titleZh,
            String titleEn,
            String descriptionZh,
            String descriptionEn,
            Integer estimateHours
    ) {
    }

    public record TaskPlanResult(
            List<TaskPlanItem> tasks,
            List<String> impactedFiles,
            List<String> riskHints,
            List<String> testHints,
            String provider,
            String model,
            String rationale
    ) {
    }
}
