package com.example.mini_project_ss14.llmops.service;

import com.example.mini_project_ss14.llmops.domain.LlmOpsDomain;
import com.example.mini_project_ss14.llmops.domain.LlmOpsTraceContext;
import com.example.mini_project_ss14.llmops.domain.LlmOpsUsage;
import com.example.mini_project_ss14.llmops.dto.LlmOpsMetricResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

@Service
public class LlmOpsMetricsService {

    private final ConcurrentMap<MetricKey, MetricAccumulator> metrics = new ConcurrentHashMap<>();

    public void recordSuccess(LlmOpsTraceContext context, LlmOpsUsage usage, long latencyMs) {
        MetricAccumulator metric = metrics.computeIfAbsent(metricKey(context), ignored -> new MetricAccumulator());
        metric.totalCalls.increment();
        metric.successfulCalls.increment();
        metric.totalLatencyMs.add(latencyMs);
        metric.inputTokens.add(usage.inputTokens());
        metric.outputTokens.add(usage.outputTokens());
        metric.totalTokens.add(usage.totalTokens());
        metric.estimatedCostUsd.add(usage.totalCostUsd());
        metric.lastUpdatedAt.set(Instant.now());
    }

    public void recordFailure(LlmOpsTraceContext context, long latencyMs) {
        MetricAccumulator metric = metrics.computeIfAbsent(metricKey(context), ignored -> new MetricAccumulator());
        metric.totalCalls.increment();
        metric.failedCalls.increment();
        metric.totalLatencyMs.add(latencyMs);
        metric.lastUpdatedAt.set(Instant.now());
    }

    public List<LlmOpsMetricResponse> getMetrics() {
        return metrics.entrySet().stream()
                .map(entry -> toResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator
                        .comparing(LlmOpsMetricResponse::getDomain)
                        .thenComparing(LlmOpsMetricResponse::getOperation))
                .toList();
    }

    private MetricKey metricKey(LlmOpsTraceContext context) {
        return new MetricKey(context.domain(), context.resolvedOperation());
    }

    private LlmOpsMetricResponse toResponse(MetricKey key, MetricAccumulator metric) {
        long totalCalls = metric.totalCalls.sum();
        double averageLatencyMs = totalCalls == 0 ? 0 : (double) metric.totalLatencyMs.sum() / totalCalls;
        return new LlmOpsMetricResponse(
                key.domain().name(),
                key.operation(),
                totalCalls,
                metric.successfulCalls.sum(),
                metric.failedCalls.sum(),
                averageLatencyMs,
                metric.inputTokens.sum(),
                metric.outputTokens.sum(),
                metric.totalTokens.sum(),
                metric.estimatedCostUsd.sum(),
                metric.lastUpdatedAt.get()
        );
    }

    private record MetricKey(LlmOpsDomain domain, String operation) {
    }

    private static class MetricAccumulator {
        private final LongAdder totalCalls = new LongAdder();
        private final LongAdder successfulCalls = new LongAdder();
        private final LongAdder failedCalls = new LongAdder();
        private final LongAdder totalLatencyMs = new LongAdder();
        private final LongAdder inputTokens = new LongAdder();
        private final LongAdder outputTokens = new LongAdder();
        private final LongAdder totalTokens = new LongAdder();
        private final DoubleAdder estimatedCostUsd = new DoubleAdder();
        private final AtomicReference<Instant> lastUpdatedAt = new AtomicReference<>();
    }
}
