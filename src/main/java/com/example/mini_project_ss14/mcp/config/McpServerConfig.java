package com.example.mini_project_ss14.mcp.config;

import com.example.mini_project_ss14.mcp.tool.DeliveryLookupTool;
import com.example.mini_project_ss14.mcp.tool.SqlQueryTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mcp-server")
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider mcpServerLocalTools(
            DeliveryLookupTool deliveryLookupTool,
            SqlQueryTool sqlQueryTool) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(deliveryLookupTool, sqlQueryTool)
                .build();
    }
}