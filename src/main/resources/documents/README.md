# Thư mục tài liệu quy chế (Documents Directory)

## Hướng dẫn

Đặt file tài liệu quy chế vận chuyển vào thư mục này.

### Yêu cầu:
- **Định dạng hỗ trợ:** PDF, Markdown (.md), Text (.txt)
- **Độ dài tối thiểu:** 3 trang A4
- **Nội dung:** Quy chế vận chuyển, chính sách bồi thường, biểu phí, thời gian giao cam kết, v.v.

### Ví dụ tên file:
- `quy_che_van_chuyen.pdf`
- `chinh_sach_boi_thuong.pdf`
- `quy_dinh_logistics.md`

### Lưu ý:
- Hệ thống sẽ đọc **tất cả** các file trong thư mục này khi chạy ingestion.
- File sẽ được extract text bằng Apache Tika, sau đó chunking và embedding vào vector_store.
- Metadata (tên tài liệu, số trang) sẽ được tự động trích xuất.

### [CẦN LÀM]
Bạn cần đặt ít nhất 1 file tài liệu quy chế thật (PDF hoặc Markdown) vào đây trước khi chạy ingestion.
