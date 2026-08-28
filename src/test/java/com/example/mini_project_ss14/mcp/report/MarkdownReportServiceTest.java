package com.example.mini_project_ss14.mcp.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownReportServiceTest {

    private MarkdownReportService service;

    @BeforeEach
    void setUp() {
        service = new MarkdownReportService();
    }

    @Test
    void shouldFormatQueryResultsAsMarkdownTable() {

        List<Map<String, Object>> rows = List.of(
                Map.of(
                        "status", "DELIVERED",
                        "total", 120
                ),
                Map.of(
                        "status", "DELAYED",
                        "total", 12
                )
        );

        String result = service.format(rows);

        assertTrue(result.contains("## Query Result"));
        assertTrue(result.contains("| status | total |"));
        assertTrue(result.contains("| DELIVERED | 120 |"));
        assertTrue(result.contains("| DELAYED | 12 |"));
        assertTrue(result.contains("**Total records:** 2"));
    }

    @Test
    void shouldHandleEmptyResults() {

        String result = service.format(List.of());

        assertEquals(
                "## Query Result\n\nNo records found.",
                result
        );
    }

    @Test
    void shouldHandleNullValues() {

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("status", "DAMAGED");
        row.put("description", null);

        List<Map<String, Object>> rows = List.of(row);

        String result = service.format(rows);

        assertTrue(result.contains("| DAMAGED |"));
        assertTrue(result.contains("**Total records:** 1"));
    }

    @Test
    void shouldEscapeMarkdownPipeCharacter() {

        List<Map<String, Object>> rows = List.of(
                Map.of(
                        "description", "Package | damaged"
                )
        );

        String result = service.format(rows);

        assertTrue(result.contains("Package \\| damaged"));
    }
}
