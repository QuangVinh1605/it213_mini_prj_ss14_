package com.example.mini_project_ss14.rag.controller;

import com.example.mini_project_ss14.rag.dto.RagResponse;
import com.example.mini_project_ss14.rag.ingestion.DocumentIngestionService;
import com.example.mini_project_ss14.rag.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.Map;

/**
 * RAG Controller — API endpoints theo SRS:
 * - GET /api/v1/rag/ask?question=... → Tra cứu quy chế
 * - POST /api/v1/rag/ingest → Nạp tài liệu vào vector store
 * - POST /api/v1/rag/ingest/upload → Upload và nạp tài liệu
 */
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final RagService ragService;
    private final DocumentIngestionService ingestionService;

    public RagController(RagService ragService, DocumentIngestionService ingestionService) {
        this.ragService = ragService;
        this.ingestionService = ingestionService;
    }

    /**
     * API tra cứu quy chế RAG theo SRS.
     * GET /api/v1/rag/ask?question=Chính sách bồi thường khi hàng bị hỏng là gì?
     *
     * @param question câu hỏi tra cứu
     * @return RagResponse gồm answer + sourceDocuments
     */
    @GetMapping("/ask")
    public ResponseEntity<RagResponse> ask(@RequestParam String question) {
        log.info("API /ask - Question: {}", question);

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new RagResponse("Câu hỏi không được để trống.", java.util.List.of())
            );
        }

        RagResponse response = ragService.ask(question.trim());
        return ResponseEntity.ok(response);
    }

    /**
     * Nạp tất cả tài liệu từ thư mục documents vào vector store.
     * POST /api/v1/rag/ingest
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestDocuments() {
        log.info("API /ingest - Bắt đầu nạp tài liệu");

        try {
            int chunksIngested = ingestionService.ingestAllDocuments();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã nạp tài liệu thành công",
                    "chunksIngested", chunksIngested
            ));
        } catch (Exception e) {
            log.error("Lỗi khi nạp tài liệu", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Lỗi khi nạp tài liệu: " + e.getMessage()
            ));
        }
    }

    /**
     * Upload file tài liệu và nạp vào vector store.
     * POST /api/v1/rag/ingest/upload
     *
     * @param file file PDF/Markdown được upload
     */
    @PostMapping("/ingest/upload")
    public ResponseEntity<Map<String, Object>> uploadAndIngest(
            @RequestParam("file") MultipartFile file) {
        log.info("API /ingest/upload - File: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "File không được để trống"
            ));
        }

        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            int chunksIngested = ingestionService.ingestAndStore(resource);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã nạp tài liệu thành công",
                    "filename", file.getOriginalFilename(),
                    "chunksIngested", chunksIngested
            ));
        } catch (IOException e) {
            log.error("Lỗi khi đọc file upload", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Lỗi khi đọc file: " + e.getMessage()
            ));
        }
    }
}
