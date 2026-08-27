package com.example.mini_project_ss14.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final ChatClient chatClient;

    public AgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Bạn là trợ lý vận hành AI của trung tâm SmartHub - RikkeiExpress.\n" +
                        "Nhiệm vụ của bạn là tiếp nhận phản ánh sự cố từ khách hàng và tự động xử lý.\n" +
                        "Hãy lịch sự bóc tách thông tin (Mã vận đơn, Loại sự cố, Bưu cục, Mức độ nghiêm trọng) từ tin nhắn của khách.\n" +
                        "Nếu khách hàng báo lỗi hỏng hóc hoặc giao trễ, thất lạc, hãy sử dụng công cụ createIncidentTool để tạo phiếu sự cố.\n" +
                        "Và sử dụng công cụ updateDeliveryStatusTool để cập nhật trạng thái đơn hàng tương ứng (DAMAGED cho hỏng hóc, DELAYED cho giao trễ).\n" +
                        "Nếu mã đơn hàng không tồn tại, công cụ sẽ trả về lỗi, hãy thông báo lại cho khách hàng một cách thân thiện, tuyệt đối không bịa đặt thông tin.\n" +
                        "Chỉ trả lời những gì liên quan đến sự cố vận chuyển và logistics.")
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .withTemperature(0.2) // Nhiệt độ thấp để Agent làm việc chính xác, ít "ảo giác" (Session 03)
                                .build()
                )
                .defaultFunctions("createIncidentTool", "updateDeliveryStatusTool") // Đăng ký Function Calling (Session 05, 06)
                .build();
    }

    public String processChat(String userMessage) {
        return this.chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
