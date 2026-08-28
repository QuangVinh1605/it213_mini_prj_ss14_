-- ==================================================
-- SmartHub - V4: Create vector_store table
-- Bảng lưu trữ vector & ngữ cảnh quy chế RAG
-- ==================================================
CREATE TABLE IF NOT EXISTS vector_store (
    id          UUID            DEFAULT uuid_generate_v4() PRIMARY KEY,
    content     TEXT            NOT NULL,
    metadata    JSON            DEFAULT '{}',
    embedding   vector(1536)    NOT NULL
);

-- HNSW Index cho Cosine Distance trên cột embedding
CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
    ON vector_store
    USING hnsw (embedding vector_cosine_ops);
