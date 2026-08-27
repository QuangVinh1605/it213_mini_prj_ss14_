package com.example.mini_project_ss14.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình RAG - đọc từ application.yaml prefix "rag"
 * Hỗ trợ 9 cấu hình chunking theo SRS:
 * - Chunk Size: 300, 500, 1000
 * - Overlap: 0%, 10%, 20%
 */
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private ChunkConfig chunk = new ChunkConfig();
    private SimilarityConfig similarity = new SimilarityConfig();
    private DocumentsConfig documents = new DocumentsConfig();

    public ChunkConfig getChunk() {
        return chunk;
    }

    public void setChunk(ChunkConfig chunk) {
        this.chunk = chunk;
    }

    public SimilarityConfig getSimilarity() {
        return similarity;
    }

    public void setSimilarity(SimilarityConfig similarity) {
        this.similarity = similarity;
    }

    public DocumentsConfig getDocuments() {
        return documents;
    }

    public void setDocuments(DocumentsConfig documents) {
        this.documents = documents;
    }

    /**
     * Tính overlap size dựa trên chunk size và overlap percentage.
     * Ví dụ: chunkSize=500, overlapPercent=10 → overlap=50
     */
    public int getOverlapSize() {
        return (int) (chunk.getSize() * chunk.getOverlapPercent() / 100.0);
    }

    public static class ChunkConfig {
        /**
         * Kích thước chunk (ký tự). SRS yêu cầu thử nghiệm: 300, 500, 1000
         */
        private int size = 500;

        /**
         * Phần trăm overlap. SRS yêu cầu thử nghiệm: 0, 10, 20
         */
        private int overlapPercent = 10;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getOverlapPercent() {
            return overlapPercent;
        }

        public void setOverlapPercent(int overlapPercent) {
            this.overlapPercent = overlapPercent;
        }
    }

    public static class SimilarityConfig {
        /**
         * Ngưỡng tương đồng cosine. Loại bỏ kết quả dưới ngưỡng này.
         */
        private double threshold = 0.7;

        /**
         * Số kết quả tìm kiếm tối đa.
         */
        private int topK = 5;

        public double getThreshold() {
            return threshold;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }
    }

    public static class DocumentsConfig {
        /**
         * Đường dẫn thư mục chứa tài liệu quy chế.
         */
        private String path = "classpath:documents/";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
