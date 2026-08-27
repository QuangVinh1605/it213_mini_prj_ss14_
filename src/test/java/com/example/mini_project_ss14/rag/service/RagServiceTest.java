package com.example.mini_project_ss14.rag.service;

import com.example.mini_project_ss14.rag.config.RagProperties;
import com.example.mini_project_ss14.rag.dto.RagResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho RagService.
 * Kiểm tra: RAG query, retrieval, answer, citation, anti-hallucination.
 */
@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private ChatClient ragChatClient;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private VectorStore vectorStore;

    private RagProperties ragProperties;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragProperties = new RagProperties();
        ragProperties.getSimilarity().setThreshold(0.7);
        ragProperties.getSimilarity().setTopK(5);
        ragService = new RagService(ragChatClient, vectorStore, ragProperties);
    }

    @Test
    @DisplayName("RAG trả lời câu hỏi có thông tin - Answer + Citation")
    void shouldReturnAnswerWithCitation() {
        // Given: mock ChatClient chain
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(ragChatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Theo Điều 5, chính sách bồi thường...");

        // Mock vector store trả về documents có metadata
        Document doc = new Document("Nội dung quy chế bồi thường",
                Map.of("document", "quy_che.pdf", "page", "3", "section", "Điều 5"));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        // When
        RagResponse response = ragService.ask("Chính sách bồi thường là gì?");

        // Then
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertFalse(response.getAnswer().isEmpty(), "Answer không được rỗng");
        assertFalse(response.getSourceDocuments().isEmpty(), "Phải có sourceDocuments");

        // Verify citation
        RagResponse.SourceDocument source = response.getSourceDocuments().get(0);
        assertEquals("quy_che.pdf", source.getDocument());
        assertEquals("3", source.getPage());
        assertEquals("Điều 5", source.getSection());
    }

    @Test
    @DisplayName("RAG xử lý lỗi gracefully - không crash")
    void shouldHandleErrorsGracefully() {
        // Given: ChatClient throw exception
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(ragChatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("API Error"));

        // When
        RagResponse response = ragService.ask("Test question");

        // Then: không crash, trả về error message
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertTrue(response.getAnswer().contains("lỗi") || response.getAnswer().contains("Lỗi"),
                "Phải trả về thông báo lỗi thân thiện");
    }

    @Test
    @DisplayName("RAG trả về sources rỗng khi VectorStore không có kết quả")
    void shouldReturnEmptySourcesWhenNoResults() {
        // Given
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        when(ragChatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Tôi không tìm thấy thông tin trong tài liệu");

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        // When
        RagResponse response = ragService.ask("Câu hỏi không liên quan đến quy chế?");

        // Then
        assertNotNull(response);
        assertTrue(response.getSourceDocuments().isEmpty(),
                "Không có sources khi VectorStore rỗng");
    }
}
