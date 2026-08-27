package com.example.mini_project_ss14.rag.dto;

import java.util.List;

/**
 * DTO phản hồi RAG API.
 * Chứa câu trả lời và danh sách nguồn trích dẫn (sourceDocuments).
 */
public class RagResponse {

    private String answer;
    private List<SourceDocument> sourceDocuments;

    public RagResponse() {
    }

    public RagResponse(String answer, List<SourceDocument> sourceDocuments) {
        this.answer = answer;
        this.sourceDocuments = sourceDocuments;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<SourceDocument> getSourceDocuments() {
        return sourceDocuments;
    }

    public void setSourceDocuments(List<SourceDocument> sourceDocuments) {
        this.sourceDocuments = sourceDocuments;
    }

    /**
     * Thông tin nguồn trích dẫn theo SRS:
     * - document: tên tài liệu
     * - page: số trang
     * - section: số điều/khoản (nếu có)
     */
    public static class SourceDocument {
        private String document;
        private String page;
        private String section;
        private String content;

        public SourceDocument() {
        }

        public SourceDocument(String document, String page, String section, String content) {
            this.document = document;
            this.page = page;
            this.section = section;
            this.content = content;
        }

        public String getDocument() {
            return document;
        }

        public void setDocument(String document) {
            this.document = document;
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
