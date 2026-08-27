package com.example.mini_project_ss14.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Service
public class AgentService {

    private final ChatClient agentChatClient;

    public AgentService(ChatClient agentChatClient) {
        this.agentChatClient = agentChatClient;
    }

    public String processChat(String conversationId, String userMessage) {
        return this.agentChatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .content();
    }
}
