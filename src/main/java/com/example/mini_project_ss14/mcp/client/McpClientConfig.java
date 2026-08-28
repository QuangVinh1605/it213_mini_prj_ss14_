package com.example.mini_project_ss14.mcp.client;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mcp-client-test")
public class McpClientConfig {
    @Bean
    public ToolCallbackProvider mcpClientTools(ToolCallbackProvider mcpToolCallbackProvider) {
        return mcpToolCallbackProvider;
    }
}