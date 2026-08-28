package com.example.mini_project_ss14.llmops.dto;

import java.time.Instant;

public class LlmOpsMetricResponse {

    private String domain;
    private String operation;
    private long totalCalls;
    private long successfulCalls;
    private long failedCalls;
    private double averageLatencyMs;
    private long inputTokens;
    private long outputTokens;
    private long totalTokens;
    private double estimatedCostUsd;
    private Instant lastUpdatedAt;

    public LlmOpsMetricResponse() {
    }

    public LlmOpsMetricResponse(
            String domain,
            String operation,
            long totalCalls,
            long successfulCalls,
            long failedCalls,
            double averageLatencyMs,
            long inputTokens,
            long outputTokens,
            long totalTokens,
            double estimatedCostUsd,
            Instant lastUpdatedAt) {
        this.domain = domain;
        this.operation = operation;
        this.totalCalls = totalCalls;
        this.successfulCalls = successfulCalls;
        this.failedCalls = failedCalls;
        this.averageLatencyMs = averageLatencyMs;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
        this.estimatedCostUsd = estimatedCostUsd;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public long getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(long totalCalls) {
        this.totalCalls = totalCalls;
    }

    public long getSuccessfulCalls() {
        return successfulCalls;
    }

    public void setSuccessfulCalls(long successfulCalls) {
        this.successfulCalls = successfulCalls;
    }

    public long getFailedCalls() {
        return failedCalls;
    }

    public void setFailedCalls(long failedCalls) {
        this.failedCalls = failedCalls;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(double averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(long inputTokens) {
        this.inputTokens = inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(long outputTokens) {
        this.outputTokens = outputTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(long totalTokens) {
        this.totalTokens = totalTokens;
    }

    public double getEstimatedCostUsd() {
        return estimatedCostUsd;
    }

    public void setEstimatedCostUsd(double estimatedCostUsd) {
        this.estimatedCostUsd = estimatedCostUsd;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
