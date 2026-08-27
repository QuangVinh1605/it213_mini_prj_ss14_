package com.example.mini_project_ss14.agent.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CreateIncidentRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("Mã vận đơn cần báo cáo sự cố (ví dụ: RK-2026-001)")
        String trackingCode,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Loại sự cố phát sinh (HỎNG_HÓC, GIAO_TRỄ, THẤT_LẠC)")
        String incidentType,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Mã bưu cục hoặc kho xử lý (ví dụ: HN-01, SG-02)")
        String hubCode,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Mức độ nghiêm trọng của sự cố (LOW, MEDIUM, CRITICAL)")
        String severity,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Mô tả chi tiết sự cố dựa trên thông tin người dùng cung cấp")
        String description
) {}
