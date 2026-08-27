package com.example.mini_project_ss14.agent.service;

import com.example.mini_project_ss14.llmops.domain.LlmOpsDomain;
import com.example.mini_project_ss14.llmops.domain.LlmOpsTraceContext;
import com.example.mini_project_ss14.llmops.service.LlmOpsService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final ChatClient agentChatClient;
    private final LlmOpsService llmOpsService;

    public AgentService(ChatClient agentChatClient, LlmOpsService llmOpsService) {
        this.agentChatClient = agentChatClient;
        this.llmOpsService = llmOpsService;
    }

    public String processChat(String conversationId, String userMessage) {
        return llmOpsService.traceGeneration(
                new LlmOpsTraceContext(
                        LlmOpsDomain.AGENT,
                        "agent.chat",
                        "smarthub-agent",
                        "agent-chat-generation",
                        conversationId,
                        conversationId,
                        userMessage
                ),
                () -> this.agentChatClient.prompt()
                        .user(userMessage)
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .call()
                        .content()
        );
    }
}
