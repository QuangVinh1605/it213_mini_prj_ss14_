package com.example.mini_project_ss14.mcp.report;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MarkdownReportService {

    public String format(List<Map<String, Object>> rows) {

        if (rows == null || rows.isEmpty()) {
            return "## Query Result\n\nNo records found.";
        }

        List<String> columns = rows.get(0).keySet()
                .stream()
                .sorted()
                .toList();

        StringBuilder markdown = new StringBuilder();

        markdown.append("## Query Result\n\n");

        markdown.append("| ");

        for (String column : columns) {
            markdown.append(formatColumnName(column)).append(" | ");
        }

        markdown.append("\n");

        markdown.append("| ");

        for (String ignored : columns) {
            markdown.append("--- | ");
        }

        markdown.append("\n");

        for (Map<String, Object> row : rows) {

            markdown.append("| ");

            for (String column : columns) {
                markdown.append(
                        formatValue(row.get(column))
                ).append(" | ");
            }

            markdown.append("\n");
        }

        markdown.append("\n**Total records:** ")
                .append(rows.size());

        return markdown.toString();
    }

    private String formatColumnName(String column) {
        return column
                .replace("_", " ")
                .trim();
    }

    private String formatValue(Object value) {

        if (value == null) {
            return "";
        }

        String text = value.toString();

        return text.replace("|", "\\|")
                .replace("\n", " ");
    }
}
