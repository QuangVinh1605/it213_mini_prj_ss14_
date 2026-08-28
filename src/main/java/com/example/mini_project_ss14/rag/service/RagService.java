package com.example.mini_project_ss14.rag.service;

import com.example.mini_project_ss14.rag.config.RagProperties;
import com.example.mini_project_ss14.rag.dto.RagResponse;
import com.example.mini_project_ss14.rag.dto.RagResponse.SourceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RAG Service — Xử lý câu hỏi tra cứu quy chế.
 * Pipeline: Question → Similarity Search → QuestionAnswerAdvisor → LLM → Answer + Citation
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final ChatClient ragChatClient;
    private final VectorStore vectorStore;
    private final RagProperties ragProperties;

    public RagService(
            @Qualifier("ragChatClient") ChatClient ragChatClient,
            VectorStore vectorStore,
            RagProperties ragProperties) {
        this.ragChatClient = ragChatClient;
        this.vectorStore = vectorStore;
        this.ragProperties = ragProperties;
    }

    /**
     * Trả lời câu hỏi dựa trên tài liệu quy chế (RAG).
     *
     * @param question câu hỏi từ người dùng
     * @return RagResponse chứa answer và sourceDocuments
     */
    public RagResponse ask(String question) {
        log.info("RAG Question: {}", question);

        try {
            // 1. Gọi ChatClient với QuestionAnswerAdvisor (đã cấu hình sẵn)
            // QuestionAnswerAdvisor sẽ tự động:
            //   - Tìm kiếm tương đồng trong VectorStore
            //   - Inject context vào prompt
            //   - Gọi LLM sinh câu trả lời
            String answer = ragChatClient.prompt()
                    .user(question)
                    .call()
                    .content();

            // 2. Truy vấn riêng để lấy sourceDocuments cho citation
            List<SourceDocument> sources = retrieveSources(question);

            log.info("RAG Answer (truncated): {}...", answer != null && answer.length() > 100
                    ? answer.substring(0, 100) : answer);
            log.info("Sources found: {}", sources.size());

            return new RagResponse(answer, sources);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý RAG question", e);
            return new RagResponse(
                    "Đã xảy ra lỗi khi xử lý câu hỏi. Vui lòng thử lại sau.",
                    List.of()
            );
        }
    }

    /**
     * Truy vấn VectorStore để lấy danh sách sourceDocuments cho citation.
     */
    private List<SourceDocument> retrieveSources(String question) {
        List<SourceDocument> sources = new ArrayList<>();

        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .similarityThreshold(ragProperties.getSimilarity().getThreshold())
                    .topK(ragProperties.getSimilarity().getTopK())
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);

            for (Document doc : results) {
                Map<String, Object> metadata = doc.getMetadata();
                sources.add(new SourceDocument(
                        getMetadataString(metadata, "document"),
                        getMetadataString(metadata, "page"),
                        getMetadataString(metadata, "section"),
                        truncateContent(doc.getText(), 200)
                ));
            }
        } catch (Exception e) {
            log.warn("Không thể truy vấn sources cho citation", e);
        }

        return sources;
    }

    /**
     * Lấy giá trị metadata dưới dạng String, trả về chuỗi rỗng nếu không có.
     */
    private String getMetadataString(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Cắt ngắn nội dung để tránh response quá dài.
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
}
