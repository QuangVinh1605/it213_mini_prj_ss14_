package com.example.mini_project_ss14.rag.ingestion;

import com.example.mini_project_ss14.rag.config.RagProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Document Ingestion Service — Pipeline theo SRS:
 * PDF/Markdown → Apache Tika → Extract Text → Chunking → Embedding → Pgvector
 *
 * Hỗ trợ metadata citation: document name, page, section.
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;
    private final RagProperties ragProperties;

    public DocumentIngestionService(VectorStore vectorStore, RagProperties ragProperties) {
        this.vectorStore = vectorStore;
        this.ragProperties = ragProperties;
    }

    /**
     * Nạp tất cả tài liệu từ thư mục documents vào vector store.
     *
     * @return số lượng chunks đã nạp
     */
    public int ingestAllDocuments() {
        log.info("=== Bắt đầu Document Ingestion ===");
        log.info("Chunk size: {}, Overlap: {}% ({})",
                ragProperties.getChunk().getSize(),
                ragProperties.getChunk().getOverlapPercent(),
                ragProperties.getOverlapSize());

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Document> allChunks = new ArrayList<>();

        try {
            // Tìm tất cả file PDF và Markdown trong thư mục documents
            Resource[] pdfResources = resolver.getResources("classpath:documents/*.pdf");
            Resource[] mdResources = resolver.getResources("classpath:documents/*.md");
            Resource[] txtResources = resolver.getResources("classpath:documents/*.txt");

            Resource[][] allResources = {pdfResources, mdResources, txtResources};

            for (Resource[] resources : allResources) {
                for (Resource resource : resources) {
                    // Bỏ qua file README.md
                    if (resource.getFilename() != null && resource.getFilename().equals("README.md")) {
                        continue;
                    }
                    try {
                        List<Document> chunks = ingestDocument(resource);
                        allChunks.addAll(chunks);
                    } catch (Exception e) {
                        log.error("Lỗi khi nạp tài liệu: {}", resource.getFilename(), e);
                    }
                }
            }

        } catch (IOException e) {
            log.error("Lỗi khi đọc thư mục documents", e);
            throw new RuntimeException("Không thể đọc thư mục documents", e);
        }

        if (!allChunks.isEmpty()) {
            vectorStore.add(allChunks);
            log.info("=== Hoàn thành Ingestion: {} chunks đã nạp vào vector store ===", allChunks.size());
        } else {
            log.warn("Không tìm thấy tài liệu nào để nạp. " +
                    "Hãy đặt file PDF/Markdown vào src/main/resources/documents/");
        }

        return allChunks.size();
    }

    /**
     * Nạp một tài liệu cụ thể từ Resource.
     *
     * @param resource Spring Resource trỏ đến file tài liệu
     * @return danh sách chunks đã được tạo
     */
    public List<Document> ingestDocument(Resource resource) {
        String filename = resource.getFilename() != null ? resource.getFilename() : "unknown";
        log.info("Đang nạp tài liệu: {}", filename);

        // 1. Apache Tika extract text
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> rawDocuments = new ArrayList<>(reader.get());
        log.info("  → Tika extract: {} document(s)", rawDocuments.size());

        // 2. Thêm metadata nguồn cho citation
        for (int i = 0; i < rawDocuments.size(); i++) {
            Document doc = rawDocuments.get(i);
            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            metadata.put("document", filename);
            metadata.put("page", String.valueOf(i + 1));
            metadata.put("source", filename);
            // section sẽ được thêm nếu tài liệu có cấu trúc điều/khoản
            if (!metadata.containsKey("section")) {
                metadata.put("section", "");
            }
            rawDocuments.set(i, new Document(doc.getText(), metadata));
        }

        // 3. Chunking với cấu hình từ properties
        TokenTextSplitter splitter = new TokenTextSplitter(
                ragProperties.getChunk().getSize(),    // defaultChunkSize
                ragProperties.getOverlapSize(),        // minChunkSizeChars (overlap)
                5,                                     // minChunkLengthToEmbed
                100,                                   // maxNumChunks
                true                                   // keepSeparator
        );

        List<Document> chunks = new ArrayList<>(splitter.apply(rawDocuments));

        // 4. Đảm bảo metadata được kế thừa cho mỗi chunk
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            Map<String, Object> chunkMeta = new HashMap<>(chunk.getMetadata());
            if (!chunkMeta.containsKey("document")) {
                chunkMeta.put("document", filename);
            }
            chunkMeta.put("chunk_index", String.valueOf(i));
            chunks.set(i, new Document(chunk.getText(), chunkMeta));
        }

        log.info("  → Chunking: {} chunk(s) (size={}, overlap={}%)",
                chunks.size(),
                ragProperties.getChunk().getSize(),
                ragProperties.getChunk().getOverlapPercent());

        return chunks;
    }

    /**
     * Nạp một tài liệu từ Resource và lưu trực tiếp vào vector store.
     *
     * @param resource Spring Resource trỏ đến file tài liệu
     * @return số lượng chunks đã nạp
     */
    public int ingestAndStore(Resource resource) {
        List<Document> chunks = ingestDocument(resource);
        if (!chunks.isEmpty()) {
            vectorStore.add(chunks);
        }
        return chunks.size();
    }
}
