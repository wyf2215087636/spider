package com.spider.apigateway.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class CodeImpactAgent {
    private static final Logger log = LoggerFactory.getLogger(CodeImpactAgent.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z0-9_\\-\\u4e00-\\u9fa5]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "from", "that", "this", "into", "about",
            "需求", "功能", "需要", "一个", "实现", "流程", "支持", "页面", "系统", "角色"
    );
    private static final Set<String> SUPPORTED_SUFFIXES = Set.of(".java", ".ts", ".tsx", ".sql", ".md", ".yml");

    public CodeImpactReport analyze(String requestText, int maxFiles) {
        Path repoRoot = detectRepoRoot();
        if (repoRoot == null) {
            return new CodeImpactReport(
                    "Repository path not detected, impact analysis is degraded.",
                    List.of(),
                    List.of("Please configure backend/frontend repository paths."),
                    List.of("Run full regression tests after manual review.")
            );
        }

        Set<String> keywords = extractKeywords(requestText);
        List<FileScore> scored = new ArrayList<>();
        List<Path> roots = List.of(
                repoRoot.resolve("backend/api-gateway/src/main/java"),
                repoRoot.resolve("backend/api-gateway/src/main/resources/db/migration"),
                repoRoot.resolve("frontend/web-console/src")
        );

        for (Path root : roots) {
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(Files::isRegularFile)
                        .filter(this::isSupportedFile)
                        .forEach(file -> scoreFile(repoRoot, keywords, file, scored));
            } catch (IOException ex) {
                log.warn("code.impact.walk.failed root={} detail={}", root, ex.getMessage());
            }
        }

        scored.sort(Comparator.comparingInt(FileScore::score).reversed());
        List<String> impactedFiles = scored.stream()
                .filter(item -> item.score() > 0)
                .limit(Math.max(1, maxFiles))
                .map(FileScore::relativePath)
                .toList();

        if (impactedFiles.isEmpty()) {
            impactedFiles = scored.stream()
                    .limit(Math.max(1, Math.min(5, maxFiles)))
                    .map(FileScore::relativePath)
                    .toList();
        }

        String summary = impactedFiles.isEmpty()
                ? "No confident impact files were identified."
                : "Potential impact touches " + impactedFiles.size() + " files across backend/frontend modules.";

        List<String> risks = new ArrayList<>();
        List<String> tests = new ArrayList<>();
        if (impactedFiles.stream().anyMatch(path -> path.contains("/db/migration/"))) {
            risks.add("Database migration may affect compatibility and rollback strategy.");
            tests.add("Run migration verification on a staging PostgreSQL 15 instance.");
        }
        if (impactedFiles.stream().anyMatch(path -> path.contains("/controller/") || path.contains("client.ts"))) {
            risks.add("API contract and frontend client may drift if not updated together.");
            tests.add("Run API smoke tests and frontend build after contract changes.");
        }
        if (risks.isEmpty()) {
            risks.add("Cross-role acceptance criteria may be incomplete without manual review.");
        }
        if (tests.isEmpty()) {
            tests.add("Add focused integration tests for the changed workflow.");
        }

        return new CodeImpactReport(summary, impactedFiles, risks, tests);
    }

    private void scoreFile(Path repoRoot, Set<String> keywords, Path file, List<FileScore> scored) {
        String relative = toUnixPath(repoRoot.relativize(file).toString());
        String lowerPath = relative.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : keywords) {
            if (lowerPath.contains(keyword)) {
                score += 8;
            }
        }

        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String lowerContent = content.toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (lowerContent.contains(keyword)) {
                    score += 2;
                }
            }
        } catch (IOException ex) {
            log.debug("code.impact.read.skip file={} detail={}", file, ex.getMessage());
        }

        if (lowerPath.contains("handoff") || lowerPath.contains("chat")) {
            score += 3;
        }
        scored.add(new FileScore(relative, score));
    }

    private boolean isSupportedFile(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String suffix : SUPPORTED_SUFFIXES) {
            if (name.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> extractKeywords(String rawText) {
        Set<String> result = new HashSet<>();
        if (rawText == null || rawText.isBlank()) {
            return result;
        }
        Matcher matcher = WORD_PATTERN.matcher(rawText.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String word = matcher.group().trim();
            if (word.length() < 3 || STOP_WORDS.contains(word)) {
                continue;
            }
            result.add(word);
            if (result.size() >= 24) {
                break;
            }
        }
        return result;
    }

    private Path detectRepoRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();
        candidates.add(current);
        if (current.getParent() != null) {
            candidates.add(current.getParent());
        }
        if (current.getParent() != null && current.getParent().getParent() != null) {
            candidates.add(current.getParent().getParent());
        }
        for (Path candidate : candidates) {
            boolean hasBackend = Files.exists(candidate.resolve("backend/api-gateway/src/main/java"));
            boolean hasFrontend = Files.exists(candidate.resolve("frontend/web-console/src"));
            if (hasBackend && hasFrontend) {
                return candidate;
            }
        }
        return null;
    }

    private String toUnixPath(String path) {
        return path.replace('\\', '/');
    }

    private record FileScore(String relativePath, int score) {
    }

    public record CodeImpactReport(
            String summary,
            List<String> impactedFiles,
            List<String> riskHints,
            List<String> testHints
    ) {
    }
}
