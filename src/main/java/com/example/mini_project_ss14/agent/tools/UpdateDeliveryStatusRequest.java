package com.example.mini_project_ss14.agent.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record UpdateDeliveryStatusRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("Mã vận đơn cần cập nhật trạng thái (ví dụ: RK-2026-001)")
        String trackingCode,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Trạng thái mới của đơn hàng (DAMAGED, DELAYED)")
        String newStatus
) {}
