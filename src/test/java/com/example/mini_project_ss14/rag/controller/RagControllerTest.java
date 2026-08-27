package com.example.mini_project_ss14.rag.controller;

import com.example.mini_project_ss14.rag.dto.RagResponse;
import com.example.mini_project_ss14.rag.ingestion.DocumentIngestionService;
import com.example.mini_project_ss14.rag.service.RagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests cho RAG API endpoints.
 */
@WebMvcTest(RagController.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RagService ragService;

    @MockBean
    private DocumentIngestionService ingestionService;

    @Test
    @DisplayName("GET /api/v1/rag/ask - Trả về answer + sourceDocuments")
    void shouldReturnRagResponse() throws Exception {
        // Given
        RagResponse response = new RagResponse(
                "Theo Điều 5 của quy chế, chính sách bồi thường...",
                List.of(new RagResponse.SourceDocument(
                        "quy_che_van_chuyen.pdf", "3", "Điều 5", "Nội dung trích dẫn..."
                ))
        );
        when(ragService.ask(anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/rag/ask")
                        .param("question", "Chính sách bồi thường là gì?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").isNotEmpty())
                .andExpect(jsonPath("$.sourceDocuments").isArray())
                .andExpect(jsonPath("$.sourceDocuments[0].document").value("quy_che_van_chuyen.pdf"))
                .andExpect(jsonPath("$.sourceDocuments[0].page").value("3"))
                .andExpect(jsonPath("$.sourceDocuments[0].section").value("Điều 5"));
    }

    @Test
    @DisplayName("GET /api/v1/rag/ask - Anti-hallucination: không tìm thấy thông tin")
    void shouldReturnNotFoundMessage() throws Exception {
        // Given
        RagResponse response = new RagResponse(
                "Tôi không tìm thấy thông tin trong tài liệu",
                List.of()
        );
        when(ragService.ask(anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/rag/ask")
                        .param("question", "Thời tiết hôm nay thế nào?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Tôi không tìm thấy thông tin trong tài liệu"))
                .andExpect(jsonPath("$.sourceDocuments").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/rag/ask - Câu hỏi rỗng trả về 400")
    void shouldReturn400ForEmptyQuestion() throws Exception {
        mockMvc.perform(get("/api/v1/rag/ask")
                        .param("question", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/rag/ingest - Nạp tài liệu thành công")
    void shouldIngestDocuments() throws Exception {
        // Given
        when(ingestionService.ingestAllDocuments()).thenReturn(15);

        // When & Then
        mockMvc.perform(post("/api/v1/rag/ingest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.chunksIngested").value(15));
    }
}
