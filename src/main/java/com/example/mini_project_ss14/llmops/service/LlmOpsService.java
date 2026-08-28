package com.example.mini_project_ss14.llmops.service;

import com.example.mini_project_ss14.llmops.config.LlmOpsProperties;
import com.example.mini_project_ss14.llmops.domain.LlmOpsTraceContext;
import com.example.mini_project_ss14.llmops.domain.LlmOpsUsage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class LlmOpsService {

    private static final Logger log = LoggerFactory.getLogger(LlmOpsService.class);
    private static final AttributeKey<List<String>> LANGFUSE_TRACE_TAGS =
            AttributeKey.stringArrayKey("langfuse.trace.tags");

    private final LlmOpsProperties properties;
    private final InfiniteLoopGuard infiniteLoopGuard;
    private final LlmOpsMetricsService metricsService;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public LlmOpsService(
            LlmOpsProperties properties,
            InfiniteLoopGuard infiniteLoopGuard,
            LlmOpsMetricsService metricsService,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.infiniteLoopGuard = infiniteLoopGuard;
        this.metricsService = metricsService;
        this.objectMapper = objectMapper;
        this.tracer = GlobalOpenTelemetry.getTracer("smarthub-llmops");
    }

    public String traceGeneration(LlmOpsTraceContext context, Supplier<String> generationCall) {
        if (!properties.isEnabled()) {
            return generationCall.get();
        }

        long startNanos = System.nanoTime();
        Span span = tracer.spanBuilder(context.resolvedObservationName())
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("langfuse.trace.name", context.resolvedTraceName())
                .setAttribute("langfuse.observation.type", "generation")
                .setAttribute("langfuse.observation.level", "DEFAULT")
                .setAttribute("langfuse.observation.model.name", properties.getModel())
                .setAttribute("langfuse.user.id", context.resolvedUserId())
                .setAttribute("langfuse.session.id", context.resolvedSessionId())
                .setAttribute("langfuse.observation.input", payload("content", context.input()))
                .setAttribute("langfuse.observation.metadata.domain", context.domain().name())
                .setAttribute("langfuse.observation.metadata.operation", context.resolvedOperation())
                .setAttribute("langfuse.observation.metadata.guard_key", context.guardKey())
                .setAttribute("gen_ai.system", properties.getProvider())
                .setAttribute("gen_ai.operation.name", "chat")
                .setAttribute("gen_ai.request.model", properties.getModel())
                .setAttribute(LANGFUSE_TRACE_TAGS, List.of("smarthub", context.domain().name().toLowerCase(), "llmops"))
                .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            int callsInWindow = infiniteLoopGuard.registerCall(context.guardKey());
            span.setAttribute("smarthub.llm.guard.calls_in_window", callsInWindow);
            span.setAttribute("langfuse.observation.completion_start_time", Instant.now().toString());

            String output = generationCall.get();
            long latencyMs = elapsedMillis(startNanos);
            LlmOpsUsage usage = estimateUsage(context.input(), output);

            span.setAttribute("langfuse.observation.output", payload("content", output));
            span.setAttribute("langfuse.observation.usage_details", usagePayload(usage));
            span.setAttribute("langfuse.observation.cost_details", costPayload(usage));
            span.setAttribute("smarthub.llm.latency_ms", latencyMs);
            span.setAttribute("gen_ai.usage.input_tokens", usage.inputTokens());
            span.setAttribute("gen_ai.usage.output_tokens", usage.outputTokens());
            span.setAttribute("gen_ai.usage.total_tokens", usage.totalTokens());

            metricsService.recordSuccess(context, usage, latencyMs);
            log.debug("LLMOps traced {} in {} ms, estimatedTokens={}, estimatedCostUsd={}",
                    context.resolvedOperation(), latencyMs, usage.totalTokens(), usage.totalCostUsd());
            return output;
        } catch (RuntimeException ex) {
            long latencyMs = elapsedMillis(startNanos);
            String statusMessage = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            span.setStatus(StatusCode.ERROR, statusMessage);
            span.setAttribute("langfuse.observation.level", "ERROR");
            span.setAttribute("langfuse.observation.status_message", statusMessage);
            span.recordException(ex);
            metricsService.recordFailure(context, latencyMs);
            throw ex;
        } finally {
            span.end();
        }
    }

    private LlmOpsUsage estimateUsage(String input, String output) {
        long inputTokens = estimateTokens(input);
        long outputTokens = estimateTokens(output);
        long totalTokens = inputTokens + outputTokens;
        double inputCost = inputTokens * properties.getCost().getInputUsdPer1kTokens() / 1000.0;
        double outputCost = outputTokens * properties.getCost().getOutputUsdPer1kTokens() / 1000.0;
        return new LlmOpsUsage(
                inputTokens,
                outputTokens,
                totalTokens,
                inputCost,
                outputCost,
                inputCost + outputCost
        );
    }

    private long estimateTokens(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        int charsPerToken = Math.max(1, properties.getToken().getEstimatedCharsPerToken());
        return Math.max(1, (long) Math.ceil((double) text.length() / charsPerToken));
    }

    private long elapsedMillis(long startNanos) {
        return Math.max(0, (System.nanoTime() - startNanos) / 1_000_000);
    }

    private String payload(String key, String value) {
        if (!properties.isCapturePayloads()) {
            return toJson(Map.of(key, "[payload capture disabled]"));
        }
        return toJson(Map.of(key, truncate(value)));
    }

    private String usagePayload(LlmOpsUsage usage) {
        return toJson(Map.of(
                "input", usage.inputTokens(),
                "output", usage.outputTokens(),
                "total", usage.totalTokens()
        ));
    }

    private String costPayload(LlmOpsUsage usage) {
        return toJson(Map.of(
                "input", usage.inputCostUsd(),
                "output", usage.outputCostUsd(),
                "total", usage.totalCostUsd()
        ));
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        int maxChars = Math.max(0, properties.getMaxPayloadChars());
        if (maxChars == 0 || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "...";
    }

    private String toJson(Map<String, ?> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            return values.toString();
        }
    }
}
