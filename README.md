# SmartHub — Trung Tâm Vận Hành Logistics Thông Minh Tích Hợp AI

## Tổng quan

SmartHub là hệ thống hỗ trợ vận tải thông minh tích hợp AI, phục vụ doanh nghiệp logistics RikkeiExpress.

Hệ thống gồm 4 phân hệ:

| Module | Chức năng | Phụ trách |
|--------|-----------|-----------|
| **Module 1: RAG** | Tra cứu tri thức quy chế | Nguyễn Quang Vinh |
| **Module 2: Agent** | Điều phối sự cố tự động (Function Calling) | Nguyễn Thạc Hưng |
| **Module 3: MCP** | Phân tích dữ liệu (Model Context Protocol) | Đỗ Minh Tuyến |
| **Module 4: LLMOps** | Giám sát & Quản trị (Langfuse Observability) | Hoàng Văn Lương |

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.13**
- **Spring AI 1.0.0**
- **PostgreSQL + Pgvector**
- **Gradle 8.12**
- **Apache Tika** (document parsing)
- **Flyway** (database migration)

---

## Hướng dẫn chạy project

### 1. Clone project

```bash
git clone <repository-url>
cd mini_project_ss14
```

### 2. Cấu hình Environment Variables

```bash
cp .env.example .env
```

Mở file `.env` và điền các giá trị:

| Biến | Mô tả | Bắt buộc |
|------|--------|----------|
| `OPENAI_API_KEY` | API key OpenAI (Embedding + Chat) | ✅ CẦN ĐIỀN |
| `DATABASE_PASSWORD` | Password PostgreSQL | ✅ CẦN ĐIỀN |
| `DATABASE_NAME` | Tên database (mặc định: smarthub) | Tùy chọn |
| `DATABASE_USERNAME` | Username database (mặc định: smarthub) | Tùy chọn |
| `DATABASE_HOST` | Host database (mặc định: localhost) | Tùy chọn |
| `DATABASE_PORT` | Port database (mặc định: 5432) | Tùy chọn |
| `OPENAI_CHAT_MODEL` | Model LLM (mặc định: gpt-4o-mini) | Tùy chọn |
| `OPENAI_EMBEDDING_MODEL` | Model Embedding (mặc định: text-embedding-3-small) | Tùy chọn |
| `RAG_CHUNK_SIZE` | Chunk size (mặc định: 500) | Tùy chọn |
| `RAG_CHUNK_OVERLAP_PERCENT` | Overlap % (mặc định: 10) | Tùy chọn |
| `RAG_SIMILARITY_THRESHOLD` | Similarity threshold (mặc định: 0.7) | Tùy chọn |
| `RAG_TOP_K` | Top-K results (mặc định: 5) | Tùy chọn |

### 3. Khởi động PostgreSQL + Pgvector

```bash
# Load biến môi trường từ .env
export $(cat .env | grep -v '^#' | xargs)

# Chạy PostgreSQL via Docker Compose
docker-compose up -d
```

Kiểm tra PostgreSQL đã sẵn sàng:
```bash
docker-compose ps
```

### 4. Chạy Spring Boot

```bash
# Load biến môi trường
export $(cat .env | grep -v '^#' | xargs)

# Chạy ứng dụng
./gradlew bootRun
```

Flyway sẽ tự động chạy migration khi khởi động.

### 5. Chuẩn bị tài liệu quy chế

Đặt file tài liệu PDF/Markdown vào:
```
src/main/resources/documents/
```

**Yêu cầu:**
- Tối thiểu 3 trang A4
- Định dạng: PDF hoặc Markdown
- Nội dung: quy chế vận chuyển, chính sách bồi thường, v.v.

### 6. Nạp tài liệu vào Vector Store (Ingestion)

```bash
# Gọi API ingestion
curl -X POST http://localhost:8080/api/v1/rag/ingest
```

Hoặc upload file trực tiếp:
```bash
curl -X POST -F "file=@path/to/document.pdf" http://localhost:8080/api/v1/rag/ingest/upload
```

### 7. Gọi RAG API

```bash
curl "http://localhost:8080/api/v1/rag/ask?question=Chính%20sách%20bồi%20thường%20khi%20hàng%20bị%20hỏng%20là%20gì?"
```

**Response mẫu:**
```json
{
  "answer": "Theo Điều 5 của quy chế vận chuyển...",
  "sourceDocuments": [
    {
      "document": "quy_che_van_chuyen.pdf",
      "page": "3",
      "section": "Điều 5",
      "content": "Nội dung trích dẫn..."
    }
  ]
}
```

---

## RAG Pipeline

```
PDF/Markdown
    ↓
Apache Tika (Extract Text)
    ↓
Chunking (configurable: 300/500/1000 x 0%/10%/20%)
    ↓
OpenAI text-embedding-3-small (1536 dimensions)
    ↓
Pgvector (Cosine Similarity + HNSW Index)
    ↓
Similarity Threshold (configurable)
    ↓
QuestionAnswerAdvisor (Spring AI)
    ↓
LLM (gpt-4o-mini)
    ↓
Answer + Citation (sourceDocuments)
```

## Thử nghiệm Chunking (9 cấu hình)

Thay đổi biến môi trường để thử nghiệm:

| Cấu hình | RAG_CHUNK_SIZE | RAG_CHUNK_OVERLAP_PERCENT |
|-----------|----------------|---------------------------|
| 1 | 300 | 0 |
| 2 | 300 | 10 |
| 3 | 300 | 20 |
| 4 | 500 | 0 |
| 5 | 500 | 10 |
| 6 | 500 | 20 |
| 7 | 1000 | 0 |
| 8 | 1000 | 10 |
| 9 | 1000 | 20 |

---

## API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|--------|
| GET | `/api/v1/rag/ask?question=...` | Tra cứu quy chế RAG |
| POST | `/api/v1/rag/ingest` | Nạp tất cả tài liệu từ thư mục documents |
| POST | `/api/v1/rag/ingest/upload` | Upload và nạp file tài liệu |

---

## Database Schema

### Bảng `deliveries`
Quản lý thông tin đơn hàng. Status: `IN_TRANSIT`, `DELIVERED`, `DELAYED`, `DAMAGED`.

### Bảng `incidents`
Phiếu sự cố do Agent khởi tạo. Incident type: `HỎNG_HÓC`, `GIAO_TRỄ`, `THẤT_LẠC`.

### Bảng `vector_store`
Lưu trữ vector embeddings cho RAG. Embedding dimension: `vector(1536)`.

---

## Chạy Tests

```bash
./gradlew test
```

---

## Cấu trúc Project

```
src/main/java/com/example/mini_project_ss14/
├── MiniProjectSs14Application.java          # Main Application
└── rag/                                      # Module 1: RAG
    ├── config/
    │   ├── RagProperties.java               # Cấu hình chunking/similarity
    │   └── RagChatClientConfig.java         # ChatClient + QuestionAnswerAdvisor
    ├── controller/
    │   └── RagController.java               # REST API endpoints
    ├── dto/
    │   └── RagResponse.java                 # Response DTO với citation
    ├── ingestion/
    │   └── DocumentIngestionService.java    # Document ingestion pipeline
    └── service/
        └── RagService.java                  # RAG query service

src/main/resources/
├── application.yaml                          # Application configuration
├── documents/                                # [CẦN THÊM FILE TÀI LIỆU]
│   └── README.md
└── db/migration/
    ├── V1__enable_extensions.sql
    ├── V2__create_deliveries_table.sql
    ├── V3__create_incidents_table.sql
    └── V4__create_vector_store_table.sql
```

---

## Lưu ý cho thành viên khác

- **KHÔNG sửa** code trong package `rag/` trừ khi thảo luận trước.
- Tạo package riêng cho module của mình (ví dụ: `agent/`, `mcp/`, `observability/`).
- Sử dụng `.env` cho tất cả secrets — KHÔNG hardcode.
- Flyway migration mới phải đặt số version tiếp theo (V5, V6...).
