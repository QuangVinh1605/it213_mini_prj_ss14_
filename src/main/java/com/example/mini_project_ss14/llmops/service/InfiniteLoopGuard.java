package com.example.mini_project_ss14.llmops.service;

import com.example.mini_project_ss14.llmops.config.LlmOpsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InfiniteLoopGuard {

    private final LlmOpsProperties properties;
    private final Clock clock;
    private final ConcurrentMap<String, LoopWindow> windows = new ConcurrentHashMap<>();

    @Autowired
    public InfiniteLoopGuard(LlmOpsProperties properties) {
        this(properties, Clock.systemUTC());
    }

    InfiniteLoopGuard(LlmOpsProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public int registerCall(String key) {
        if (!properties.getGuard().isEnabled()) {
            return 0;
        }

        long now = clock.millis();
        long windowMillis = Math.max(1, properties.getGuard().getWindowSeconds()) * 1000;
        int maxCalls = Math.max(1, properties.getGuard().getMaxCallsPerWindow());

        LoopWindow window = windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAtMillis() >= windowMillis) {
                return new LoopWindow(now, 1);
            }
            return new LoopWindow(current.startedAtMillis(), current.callCount() + 1);
        });

        if (window.callCount() > maxCalls) {
            throw new InfiniteLoopGuardException(
                    "LLMOps guard blocked repeated LLM calls for " + key
                            + " after " + maxCalls + " calls in " + properties.getGuard().getWindowSeconds() + "s"
            );
        }

        return window.callCount();
    }

    public int getActiveWindowCount() {
        return windows.size();
    }

    public int reset() {
        int size = windows.size();
        windows.clear();
        return size;
    }

    private record LoopWindow(long startedAtMillis, int callCount) {
    }
}
