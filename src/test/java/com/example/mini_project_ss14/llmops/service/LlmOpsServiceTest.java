package com.example.mini_project_ss14.llmops.service;

import com.example.mini_project_ss14.llmops.config.LlmOpsProperties;
import com.example.mini_project_ss14.llmops.domain.LlmOpsDomain;
import com.example.mini_project_ss14.llmops.domain.LlmOpsTraceContext;
import com.example.mini_project_ss14.llmops.dto.LlmOpsMetricResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LlmOpsServiceTest {

    private LlmOpsProperties properties;
    private InfiniteLoopGuard infiniteLoopGuard;
    private LlmOpsMetricsService metricsService;
    private LlmOpsService llmOpsService;

    @BeforeEach
    void setUp() {
        properties = new LlmOpsProperties();
        properties.getGuard().setMaxCallsPerWindow(2);
        properties.getGuard().setWindowSeconds(60);
        infiniteLoopGuard = new InfiniteLoopGuard(properties);
        metricsService = new LlmOpsMetricsService();
        llmOpsService = new LlmOpsService(properties, infiniteLoopGuard, metricsService, new ObjectMapper());
    }

    @Test
    @DisplayName("LLMOps ghi nhận latency, token và cost estimate cho generation")
    void shouldRecordMetricsForGeneration() {
        LlmOpsTraceContext context = context("session-1", "Xin chào");

        String output = llmOpsService.traceGeneration(context, () -> "Kết quả trả lời");

        assertEquals("Kết quả trả lời", output);
        List<LlmOpsMetricResponse> metrics = metricsService.getMetrics();
        assertEquals(1, metrics.size());
        assertEquals("AGENT", metrics.get(0).getDomain());
        assertEquals("agent.chat", metrics.get(0).getOperation());
        assertEquals(1, metrics.get(0).getTotalCalls());
        assertEquals(1, metrics.get(0).getSuccessfulCalls());
        assertTrue(metrics.get(0).getTotalTokens() > 0);
        assertTrue(metrics.get(0).getEstimatedCostUsd() > 0);
    }

    @Test
    @DisplayName("Infinite loop guard chặn quá số lần gọi LLM trong cùng cửa sổ")
    void shouldBlockRepeatedCallsWithinGuardWindow() {
        LlmOpsTraceContext context = context("session-loop", "test");

        assertDoesNotThrow(() -> llmOpsService.traceGeneration(context, () -> "ok-1"));
        assertDoesNotThrow(() -> llmOpsService.traceGeneration(context, () -> "ok-2"));

        InfiniteLoopGuardException exception = assertThrows(
                InfiniteLoopGuardException.class,
                () -> llmOpsService.traceGeneration(context, () -> "ok-3")
        );
        assertTrue(exception.getMessage().contains("LLMOps guard blocked"));

        LlmOpsMetricResponse metric = metricsService.getMetrics().get(0);
        assertEquals(3, metric.getTotalCalls());
        assertEquals(2, metric.getSuccessfulCalls());
        assertEquals(1, metric.getFailedCalls());
    }

    private LlmOpsTraceContext context(String sessionId, String input) {
        return new LlmOpsTraceContext(
                LlmOpsDomain.AGENT,
                "agent.chat",
                "smarthub-agent",
                "agent-chat-generation",
                sessionId,
                sessionId,
                input
        );
    }
}
