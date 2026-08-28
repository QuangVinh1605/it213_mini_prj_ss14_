-- ==================================================
-- SmartHub - V2: Create deliveries table
-- Bảng quản lý thông tin đơn hàng & vận chuyển
-- ==================================================
CREATE TABLE IF NOT EXISTS deliveries (
    id              BIGSERIAL       PRIMARY KEY,
    tracking_code   VARCHAR(50)     UNIQUE NOT NULL,
    customer_name   VARCHAR(100)    NOT NULL,
    hub_code        VARCHAR(20)     NOT NULL,
    status          VARCHAR(30)     NOT NULL,
    cod_amount      DECIMAL(12,2)   DEFAULT 0,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- Index trên tracking_code để tìm kiếm nhanh
CREATE INDEX IF NOT EXISTS idx_deliveries_tracking_code ON deliveries(tracking_code);
-- Index trên hub_code để phục vụ phân tích theo bưu cục
CREATE INDEX IF NOT EXISTS idx_deliveries_hub_code ON deliveries(hub_code);
-- Index trên status để lọc theo trạng thái
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
