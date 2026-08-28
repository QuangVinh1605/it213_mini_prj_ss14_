package com.example.mini_project_ss14.llmops.controller;

import com.example.mini_project_ss14.llmops.config.LlmOpsProperties;
import com.example.mini_project_ss14.llmops.dto.LlmOpsMetricResponse;
import com.example.mini_project_ss14.llmops.service.InfiniteLoopGuard;
import com.example.mini_project_ss14.llmops.service.LlmOpsMetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/llmops")
public class LlmOpsController {

    private final LlmOpsProperties properties;
    private final LlmOpsMetricsService metricsService;
    private final InfiniteLoopGuard infiniteLoopGuard;

    public LlmOpsController(
            LlmOpsProperties properties,
            LlmOpsMetricsService metricsService,
            InfiniteLoopGuard infiniteLoopGuard) {
        this.properties = properties;
        this.metricsService = metricsService;
        this.infiniteLoopGuard = infiniteLoopGuard;
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<LlmOpsMetricResponse>> metrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "enabled", properties.isEnabled(),
                "provider", properties.getProvider(),
                "model", properties.getModel(),
                "capturePayloads", properties.isCapturePayloads(),
                "langfuseOtelEndpointConfigured", System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") != null,
                "guardEnabled", properties.getGuard().isEnabled(),
                "guardMaxCallsPerWindow", properties.getGuard().getMaxCallsPerWindow(),
                "guardWindowSeconds", properties.getGuard().getWindowSeconds(),
                "activeGuardWindows", infiniteLoopGuard.getActiveWindowCount()
        ));
    }

    @DeleteMapping("/guard")
    public ResponseEntity<Map<String, Object>> resetGuard() {
        int clearedWindows = infiniteLoopGuard.reset();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "clearedWindows", clearedWindows
        ));
    }
}
