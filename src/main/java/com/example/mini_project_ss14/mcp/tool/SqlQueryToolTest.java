package com.example.mini_project_ss14.mcp.tool;

import com.example.mini_project_ss14.mcp.report.MarkdownReportService;
import com.example.mini_project_ss14.mcp.validator.SafeSqlValidator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SqlQueryToolTest {

    @Test
    void shouldRejectUnsafeQueryBeforeDatabaseExecution() {

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        SafeSqlValidator validator = new SafeSqlValidator();

        MarkdownReportService reportService =
                new MarkdownReportService();

        SqlQueryTool tool = new SqlQueryTool(
                jdbcTemplate,
                validator,
                reportService
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> tool.executeAnalysisQuery(
                        "DELETE FROM deliveries"
                )
        );

        verifyNoInteractions(jdbcTemplate);
    }
}
