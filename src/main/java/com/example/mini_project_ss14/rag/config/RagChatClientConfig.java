package com.example.mini_project_ss14.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình ChatClient tích hợp QuestionAnswerAdvisor theo SRS.
 * - Sử dụng VectorStore (PgVector) cho retrieval.
 * - Cosine Similarity với threshold có thể cấu hình.
 * - System prompt bắt buộc AI chỉ trả lời dựa trên tài liệu, trích dẫn nguồn.
 */
@Configuration
public class RagChatClientConfig {

    /**
     * System prompt cho RAG - yêu cầu AI:
     * 1. Chỉ sử dụng context từ tài liệu
     * 2. Không tự bịa thông tin
     * 3. Trích dẫn nguồn (tên tài liệu, số trang/điều khoản)
     * 4. Từ chối lịch sự nếu không tìm thấy thông tin
     */
    private static final String RAG_SYSTEM_PROMPT = """
            Bạn là trợ lý AI tra cứu quy chế vận chuyển của RikkeiExpress.

            QUY TẮC BẮT BUỘC:
            1. CHỈ trả lời dựa trên thông tin trong phần CONTEXT được cung cấp bên dưới.
            2. KHÔNG sử dụng kiến thức bên ngoài để trả lời câu hỏi về quy chế.
            3. Với mỗi thông tin trả lời, BẮT BUỘC trích dẫn nguồn gồm: tên tài liệu, số trang hoặc số điều/khoản.
            4. Nếu KHÔNG tìm thấy thông tin liên quan trong context, trả lời CHÍNH XÁC:
               "Tôi không tìm thấy thông tin trong tài liệu"
            5. KHÔNG bịa đặt, suy diễn hay thêm thông tin ngoài tài liệu.
            6. Trả lời bằng tiếng Việt, rõ ràng và chuyên nghiệp.
            """;

    @Bean("ragChatClient")
    public ChatClient ragChatClient(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore,
            RagProperties ragProperties) {

        // Cấu hình SearchRequest với similarity threshold và top-K
        SearchRequest searchRequest = SearchRequest.builder()
                .similarityThreshold(ragProperties.getSimilarity().getThreshold())
                .topK(ragProperties.getSimilarity().getTopK())
                .build();

        // Sử dụng QuestionAnswerAdvisor builder pattern (Spring AI 1.0.0)
        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();

        return chatClientBuilder
                .defaultSystem(RAG_SYSTEM_PROMPT)
                .defaultAdvisors(qaAdvisor)
                .build();
    }
}
