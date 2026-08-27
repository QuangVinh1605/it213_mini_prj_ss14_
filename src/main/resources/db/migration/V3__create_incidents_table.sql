-- ==================================================
-- SmartHub - V3: Create incidents table
-- Bảng quản lý phiếu sự cố vận hành do Agent khởi tạo
-- ==================================================
CREATE TABLE IF NOT EXISTS incidents (
    id              BIGSERIAL       PRIMARY KEY,
    tracking_code   VARCHAR(50)     NOT NULL,
    incident_type   VARCHAR(50)     NOT NULL,
    hub_code        VARCHAR(20)     NOT NULL,
    severity        VARCHAR(20)     NOT NULL,
    description     TEXT            NOT NULL,
    status          VARCHAR(30)     DEFAULT 'OPEN',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- Index trên tracking_code để tìm sự cố theo đơn hàng
CREATE INDEX IF NOT EXISTS idx_incidents_tracking_code ON incidents(tracking_code);
-- Index trên hub_code để phân tích sự cố theo bưu cục
CREATE INDEX IF NOT EXISTS idx_incidents_hub_code ON incidents(hub_code);
