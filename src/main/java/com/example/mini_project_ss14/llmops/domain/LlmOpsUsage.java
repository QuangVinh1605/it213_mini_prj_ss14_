package com.example.mini_project_ss14.llmops.domain;

public record LlmOpsUsage(
        long inputTokens,
        long outputTokens,
        long totalTokens,
        double inputCostUsd,
        double outputCostUsd,
        double totalCostUsd
) {
}
