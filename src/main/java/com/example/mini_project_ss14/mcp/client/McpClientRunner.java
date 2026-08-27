package com.example.mini_project_ss14.mcp.client;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class McpClientRunner implements CommandLineRunner {

    private final ToolCallbackProvider toolCallbackProvider;

    public McpClientRunner(ToolCallbackProvider toolCallbackProvider) {
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @Override
    public void run(String... args) {

        System.err.println("=== MCP FINAL TEST ===");

        System.err.println("\n[1] Discovering tools...");

        for (ToolCallback tool :
                toolCallbackProvider.getToolCallbacks()) {

            System.err.println(
                    "  ✓ " +
                            tool.getToolDefinition().name()
            );
        }

        System.err.println("\n[2] Testing delivery lookup...");

        ToolCallback deliveryTool =
                findTool("get_delivery_by_id");

        String deliveryResult = deliveryTool.call(
                "{\"deliveryId\":1}"
        );

        System.err.println("  Result: " + deliveryResult);

        System.err.println("\n[3] Testing SQL analysis...");

        ToolCallback sqlTool =
                findTool("execute_analysis_query");

        String sqlResult = sqlTool.call(
                """
                {
                  "sql": "SELECT status, COUNT(*) AS total FROM deliveries GROUP BY status ORDER BY total DESC"
                }
                """
        );

        System.err.println("  Result:");
        System.err.println(sqlResult);

        System.err.println("\n[4] Testing SQL security...");

        try {

            sqlTool.call(
                    """
                    {
                      "sql": "DELETE FROM deliveries"
                    }
                    """
            );

            System.err.println(
                    "  ✗ SECURITY TEST FAILED"
            );

        } catch (Exception e) {

            System.err.println(
                    "  ✓ Unsafe SQL rejected"
            );

            System.err.println(
                    "    " + e.getMessage()
            );
        }

        System.err.println("\n=== MCP FINAL TEST COMPLETE ===");
    }

    private ToolCallback findTool(String name) {

        for (ToolCallback tool :
                toolCallbackProvider.getToolCallbacks()) {

            if (tool.getToolDefinition()
                    .name()
                    .equals(name)) {

                return tool;
            }
        }

        throw new IllegalStateException(
                "MCP tool not found: " + name
        );
    }
}