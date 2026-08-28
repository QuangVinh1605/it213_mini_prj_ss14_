package com.example.mini_project_ss14.mcp.tool;

import com.example.mini_project_ss14.mcp.report.MarkdownReportService;
import com.example.mini_project_ss14.mcp.validator.SafeSqlValidator;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SqlQueryTool {

    private final JdbcTemplate jdbcTemplate;
    private final SafeSqlValidator validator;
    private final MarkdownReportService reportService;

    public SqlQueryTool(
            JdbcTemplate jdbcTemplate,
            SafeSqlValidator validator,
            MarkdownReportService reportService) {

        this.jdbcTemplate = jdbcTemplate;
        this.validator = validator;
        this.reportService = reportService;
    }

    @Tool(
            name = "execute_analysis_query",
            description = """
                Execute a read-only SQL analysis query against SmartHub logistics data.
                Only SELECT queries against the deliveries and incidents tables are allowed.
                Returns the query result as a Markdown report.
                """
    )
    public String executeAnalysisQuery(
            @ToolParam(description = "A read-only SELECT SQL query")
            String sql) {

        validator.validate(sql);

        var rows = jdbcTemplate.queryForList(sql);

        return reportService.format(rows);
    }
}