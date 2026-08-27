package com.example.mini_project_ss14.llmops.domain;

import org.springframework.util.StringUtils;

public record LlmOpsTraceContext(
        LlmOpsDomain domain,
        String operation,
        String traceName,
        String observationName,
        String sessionId,
        String userId,
        String input
) {

    public String resolvedOperation() {
        if (StringUtils.hasText(operation)) {
            return operation;
        }
        return domain.name().toLowerCase() + ".llm";
    }

    public String resolvedTraceName() {
        if (StringUtils.hasText(traceName)) {
            return traceName;
        }
        return "smarthub-" + domain.name().toLowerCase();
    }

    public String resolvedObservationName() {
        if (StringUtils.hasText(observationName)) {
            return observationName;
        }
        return resolvedOperation();
    }

    public String resolvedSessionId() {
        if (StringUtils.hasText(sessionId)) {
            return sessionId;
        }
        return "default-session";
    }

    public String resolvedUserId() {
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        return "anonymous";
    }

    public String guardKey() {
        return domain.name() + ":" + resolvedSessionId();
    }
}
