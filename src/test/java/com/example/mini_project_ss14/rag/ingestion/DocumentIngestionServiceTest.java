package com.example.mini_project_ss14.rag.ingestion;

import com.example.mini_project_ss14.rag.config.RagProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho DocumentIngestionService.
 * Kiểm tra: Tika extract, chunking, metadata, embedding pipeline.
 */
@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Captor
    private ArgumentCaptor<List<Document>> documentsCaptor;

    private DocumentIngestionService ingestionService;
    private RagProperties ragProperties;

    @BeforeEach
    void setUp() {
        ragProperties = new RagProperties();
        ragProperties.getChunk().setSize(500);
        ragProperties.getChunk().setOverlapPercent(10);
        ingestionService = new DocumentIngestionService(vectorStore, ragProperties);
    }

    @Test
    @DisplayName("Tika đọc được file text và extract text thành công")
    void shouldExtractTextFromTextResource() {
        // Given: một file text đơn giản
        String content = "Điều 1: Quy chế bồi thường hàng hóa hư hỏng trong quá trình vận chuyển. " +
                "Khách hàng được bồi thường tối đa 100% giá trị hàng hóa khai báo. " +
                "Thời hạn khiếu nại trong vòng 7 ngày kể từ ngày nhận hàng.";

        ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "test_document.txt";
            }
        };

        // When: ingest document
        List<Document> chunks = ingestionService.ingestDocument(resource);

        // Then: text được extract và chunk được tạo
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty(), "Phải có ít nhất 1 chunk");

        // Verify metadata
        Document firstChunk = chunks.get(0);
        assertEquals("test_document.txt", firstChunk.getMetadata().get("document"));
        assertNotNull(firstChunk.getText());
        assertFalse(firstChunk.getText().isEmpty());
    }

    @Test
    @DisplayName("Metadata citation được kế thừa cho mỗi chunk")
    void shouldPreserveMetadataInChunks() {
        // Given
        String content = "Điều 1: Nội dung dài để tạo nhiều chunks. ".repeat(50);

        ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "quy_che_van_chuyen.txt";
            }
        };

        // When
        List<Document> chunks = ingestionService.ingestDocument(resource);

        // Then: mỗi chunk phải có metadata document
        for (Document chunk : chunks) {
            Map<String, Object> metadata = chunk.getMetadata();
            assertEquals("quy_che_van_chuyen.txt", metadata.get("document"),
                    "Metadata 'document' phải được kế thừa");
            assertNotNull(metadata.get("page"), "Metadata 'page' phải tồn tại");
            assertNotNull(metadata.get("chunk_index"), "Metadata 'chunk_index' phải tồn tại");
        }
    }

    @Test
    @DisplayName("Chunk size thay đổi theo cấu hình")
    void shouldRespectChunkSizeConfiguration() {
        // Given: cấu hình chunk size nhỏ
        ragProperties.getChunk().setSize(100);
        ragProperties.getChunk().setOverlapPercent(0);

        String content = "Nội dung tài liệu quy chế rất dài. ".repeat(100);

        ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };

        // When
        List<Document> smallChunks = ingestionService.ingestDocument(resource);

        // Reset với chunk size lớn
        ragProperties.getChunk().setSize(1000);
        List<Document> largeChunks = ingestionService.ingestDocument(resource);

        // Then: chunk size nhỏ phải tạo ra nhiều chunks hơn
        assertTrue(smallChunks.size() >= largeChunks.size(),
                "Chunk size nhỏ phải tạo nhiều chunks hơn hoặc bằng chunk size lớn");
    }

    @Test
    @DisplayName("ingestAndStore lưu chunks vào VectorStore")
    void shouldStoreChunksInVectorStore() {
        // Given
        String content = "Điều 1: Quy chế vận chuyển hàng hóa logistics. Áp dụng cho tất cả đơn hàng.";

        ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };

        // When
        int count = ingestionService.ingestAndStore(resource);

        // Then
        assertTrue(count > 0, "Phải nạp ít nhất 1 chunk");
        verify(vectorStore, times(1)).add(documentsCaptor.capture());

        List<Document> storedDocs = documentsCaptor.getValue();
        assertFalse(storedDocs.isEmpty());
    }

    @Test
    @DisplayName("Overlap tính toán đúng theo percentage")
    void shouldCalculateOverlapCorrectly() {
        // Given
        ragProperties.getChunk().setSize(500);
        ragProperties.getChunk().setOverlapPercent(10);

        // Then
        assertEquals(50, ragProperties.getOverlapSize(), "500 * 10% = 50");

        // Given
        ragProperties.getChunk().setSize(1000);
        ragProperties.getChunk().setOverlapPercent(20);

        // Then
        assertEquals(200, ragProperties.getOverlapSize(), "1000 * 20% = 200");

        // Given
        ragProperties.getChunk().setSize(300);
        ragProperties.getChunk().setOverlapPercent(0);

        // Then
        assertEquals(0, ragProperties.getOverlapSize(), "300 * 0% = 0");
    }
}
