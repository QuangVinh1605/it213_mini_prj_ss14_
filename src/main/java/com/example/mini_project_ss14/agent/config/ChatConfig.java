package com.example.mini_project_ss14.agent.config;

import com.example.mini_project_ss14.agent.tools.AgentTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
    }

    @Bean
    public ChatClient agentChatClient(ChatClient.Builder chatClientBuilder, AgentTool agentTool) {
        return chatClientBuilder
                .defaultSystem("""
                        Bạn là trợ lý vận hành AI của trung tâm SmartHub - RikkeiExpress.
                        Nhiệm vụ của bạn là tiếp nhận phản ánh sự cố từ khách hàng và tự động xử lý.
                        Hãy bóc tách thông tin (Mã vận đơn, Loại sự cố, Bưu cục, Mức độ nghiêm trọng) từ tin nhắn của khách.
                        Khi khách hàng báo lỗi hỏng hóc hoặc giao trễ, thất lạc, hãy sử dụng createIncidentTool.
                        Và sử dụng updateDeliveryStatusTool để cập nhật trạng thái đơn hàng tương ứng (DAMAGED cho hỏng hóc, DELAYED cho giao trễ).
                        Nếu mã đơn hàng không tồn tại, công cụ sẽ trả về lỗi, hãy thông báo lại cho khách hàng một cách thân thiện, tuyệt đối không bịa đặt thông tin.
                        Chỉ trả lời những gì liên quan đến sự cố vận chuyển và logistics.
                        """)
                .defaultOptions(
                        OpenAiChatOptions.builder().withTemperature(0.2).build()
                )
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .defaultTools(agentTool)
                .build();
    }
}
