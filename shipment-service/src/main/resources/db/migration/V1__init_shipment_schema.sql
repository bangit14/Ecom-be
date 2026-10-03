-- =====================================================================
-- SHIPMENT MODULE - POSTGRESQL DATABASE SCHEMA
-- Hỗ trợ nhiều nhà vận chuyển: GHN, GHTK, Ahamove, GoShip, ...
-- =====================================================================

CREATE SCHEMA IF NOT EXISTS ecommerce_shipment;

-- ---------------------------------------------------------------------
-- 1. shipping_provider
-- Bảng master, liệt kê các nhà vận chuyển được hệ thống hỗ trợ
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipping_provider (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(50)  NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    logo_url                VARCHAR(500),
    supports_cod            BOOLEAN      NOT NULL DEFAULT TRUE,
    supports_insurance      BOOLEAN      NOT NULL DEFAULT FALSE,
    supports_webhook        BOOLEAN      NOT NULL DEFAULT TRUE,
    tracking_url_template   VARCHAR(500),
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 0a. shipping_province / shipping_district / shipping_ward
-- Bảng địa chỉ CHUNG cho tất cả provider, phân biệt bằng provider_id
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipping_province (
    id                      BIGSERIAL PRIMARY KEY,
    provider_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider(id),
    external_code           VARCHAR(50)  NOT NULL,
    province_name           VARCHAR(100) NOT NULL,
    standard_code           VARCHAR(20),

    CONSTRAINT uq_province_provider_code UNIQUE (provider_id, external_code)
);

CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipping_district (
    id                      BIGSERIAL PRIMARY KEY,
    provider_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider(id),
    province_ref_id         BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_province(id) ON DELETE CASCADE,
    external_code           VARCHAR(50)  NOT NULL,
    district_name           VARCHAR(100) NOT NULL,
    standard_code           VARCHAR(20),

    CONSTRAINT uq_district_provider_code UNIQUE (provider_id, external_code)
);

CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipping_ward (
    id                      BIGSERIAL PRIMARY KEY,
    provider_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider(id),
    district_ref_id         BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_district(id) ON DELETE CASCADE,
    external_code           VARCHAR(50)  NOT NULL,
    ward_name               VARCHAR(100) NOT NULL,
    standard_code           VARCHAR(20),

    CONSTRAINT uq_ward_provider_code UNIQUE (provider_id, external_code)
);

-- ---------------------------------------------------------------------
-- 3. shipping_provider_service
-- Mỗi provider có nhiều loại dịch vụ (giao nhanh, tiết kiệm, xe máy...)
-- Khởi tạo trước bảng config để ràng buộc FK default_service_id
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipping_provider_service (
    id                      BIGSERIAL PRIMARY KEY,
    provider_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider(id),
    service_code            VARCHAR(100) NOT NULL,
    service_name            VARCHAR(255) NOT NULL,
    max_weight_kg           NUMERIC(10,2),
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_provider_service UNIQUE (provider_id, service_code)
);

-- ---------------------------------------------------------------------
-- 2. shipping_provider_config
-- Lưu credentials/setting thực tế để gọi API từng bên
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipping_provider_config (
    id                      BIGSERIAL PRIMARY KEY,
    provider_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider(id),
    tenant_id               BIGINT,
    warehouse_id            BIGINT,

    config_name             VARCHAR(255),
    environment             VARCHAR(20)  NOT NULL DEFAULT 'PRODUCTION',

    -- Field chung
    api_base_url            VARCHAR(500),
    token                   VARCHAR(500),
    api_key                 VARCHAR(500),
    secret_key              VARCHAR(500),
    client_id               VARCHAR(255),

    -- Định danh shop/tài khoản phía provider
    provider_shop_id        VARCHAR(255),
    provider_account_name   VARCHAR(255),

    -- Địa chỉ lấy hàng mặc định
    default_pickup_name     VARCHAR(255),
    default_pickup_phone    VARCHAR(20),
    default_pickup_address  VARCHAR(500),
    default_pickup_ward     VARCHAR(100),
    default_pickup_district VARCHAR(100),
    default_pickup_province VARCHAR(100),

    -- Webhook nhận cập nhật trạng thái
    webhook_url             VARCHAR(500),
    webhook_secret          VARCHAR(255),

    -- Default order params cho người bán
    default_service_id        BIGINT       REFERENCES ecommerce_shipment.shipping_provider_service(id),
    default_payer             VARCHAR(10)  NOT NULL DEFAULT 'SHOP',
    default_insurance_enabled BOOLEAN      NOT NULL DEFAULT FALSE,
    default_weight_gram       INTEGER,

    -- Field đặc thù từng bên dạng JSONB
    extra_settings          JSONB,

    is_default              BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_spc_provider_tenant ON ecommerce_shipment.shipping_provider_config(provider_id, tenant_id);

-- ---------------------------------------------------------------------
-- 4. shipping_rate
-- Bảng giá / rate card theo provider + service
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipping_rate (
    id                      BIGSERIAL PRIMARY KEY,
    provider_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider(id),
    service_id              BIGINT       REFERENCES ecommerce_shipment.shipping_provider_service(id),

    from_province           VARCHAR(100),
    to_province             VARCHAR(100),
    weight_from_gram        INTEGER,
    weight_to_gram          INTEGER,

    base_fee                NUMERIC(15,2) NOT NULL DEFAULT 0,
    fee_per_extra_kg        NUMERIC(15,2) NOT NULL DEFAULT 0,
    cod_fee_percent         NUMERIC(5,2)  NOT NULL DEFAULT 0,
    insurance_fee_percent   NUMERIC(5,2)  NOT NULL DEFAULT 0,

    currency                CHAR(3)       NOT NULL DEFAULT 'VND',
    effective_from          DATE,
    effective_to            DATE,
    is_active               BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 5. shipment
-- Đơn vận chuyển thực tế, gắn với 1 order
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipment (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT       NOT NULL,
    provider_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider(id),
    provider_config_id      BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipping_provider_config(id),
    service_id              BIGINT       REFERENCES ecommerce_shipment.shipping_provider_service(id),

    shipment_code           VARCHAR(100) NOT NULL UNIQUE,
    external_order_code     VARCHAR(255),

    status                  VARCHAR(30)  NOT NULL DEFAULT 'PENDING',

    -- Người gửi
    sender_name             VARCHAR(255),
    sender_phone            VARCHAR(20),
    sender_address          VARCHAR(500),
    sender_ward             VARCHAR(100),
    sender_district         VARCHAR(100),
    sender_province         VARCHAR(100),

    -- Người nhận
    receiver_name           VARCHAR(255) NOT NULL,
    receiver_phone          VARCHAR(20)  NOT NULL,
    receiver_address        VARCHAR(500) NOT NULL,
    receiver_ward           VARCHAR(100),
    receiver_district       VARCHAR(100),
    receiver_province       VARCHAR(100) NOT NULL,

    cod_amount              NUMERIC(15,2) NOT NULL DEFAULT 0,
    insurance_amount        NUMERIC(15,2) NOT NULL DEFAULT 0,
    shipping_fee            NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_weight_gram       INTEGER,
    total_value             NUMERIC(15,2),

    note                    VARCHAR(1000),
    expected_delivery_at    TIMESTAMP WITHOUT TIME ZONE,
    picked_up_at            TIMESTAMP WITHOUT TIME ZONE,
    delivered_at            TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at            TIMESTAMP WITHOUT TIME ZONE,
    cancel_reason           VARCHAR(500),

    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shipment_order ON ecommerce_shipment.shipment(order_id);
CREATE INDEX IF NOT EXISTS idx_shipment_external_code ON ecommerce_shipment.shipment(external_order_code);
CREATE INDEX IF NOT EXISTS idx_shipment_status ON ecommerce_shipment.shipment(status);

-- ---------------------------------------------------------------------
-- 6. shipment_item
-- Danh sách hàng hóa trong 1 shipment
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipment_item (
    id                      BIGSERIAL PRIMARY KEY,
    shipment_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipment(id) ON DELETE CASCADE,
    order_item_id           BIGINT,

    product_name            VARCHAR(255) NOT NULL,
    sku                     VARCHAR(100),
    quantity                INTEGER      NOT NULL DEFAULT 1,
    unit_weight_gram        INTEGER,
    unit_price              NUMERIC(15,2),
    category                VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_shipment_item_shipment ON ecommerce_shipment.shipment_item(shipment_id);

-- ---------------------------------------------------------------------
-- 7. shipment_tracking
-- Trạng thái/thông tin tracking hiện tại
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipment_tracking (
    id                          BIGSERIAL PRIMARY KEY,
    shipment_id                 BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipment(id) ON DELETE CASCADE,

    tracking_number             VARCHAR(255) NOT NULL UNIQUE,
    label_url                   VARCHAR(500),
    qr_code_url                 VARCHAR(500),

    current_status              VARCHAR(50),
    current_status_description  VARCHAR(255),
    last_synced_at              TIMESTAMP WITHOUT TIME ZONE,
    sync_source                 VARCHAR(20),

    created_at                  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shipment_tracking_shipment ON ecommerce_shipment.shipment_tracking(shipment_id);

-- ---------------------------------------------------------------------
-- 8. shipment_event
-- Lịch sử toàn bộ sự kiện/trạng thái của shipment
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce_shipment.shipment_event (
    id                      BIGSERIAL PRIMARY KEY,
    shipment_id             BIGINT       NOT NULL REFERENCES ecommerce_shipment.shipment(id) ON DELETE CASCADE,
    tracking_id             BIGINT       REFERENCES ecommerce_shipment.shipment_tracking(id),

    event_code              VARCHAR(50),
    event_status            VARCHAR(50)  NOT NULL,
    event_description       VARCHAR(500),
    event_time              TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    source                  VARCHAR(20)  NOT NULL DEFAULT 'WEBHOOK',
    raw_payload             JSONB,

    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_se_shipment_time ON ecommerce_shipment.shipment_event(shipment_id, event_time);

-- ---------------------------------------------------------------------
-- Seed Initial Data for Popular Carriers (GHN, GHTK, AHAMOVE, GOSHIP)
-- ---------------------------------------------------------------------
INSERT INTO ecommerce_shipment.shipping_provider (id, code, name, logo_url, supports_cod, supports_insurance, supports_webhook, tracking_url_template, is_active)
VALUES
    (1, 'GHN', 'Giao Hàng Nhanh', 'https://cdn.haitrieu.com/wp-content/uploads/2022/05/Logo-GHN-Orange.png', true, true, true, 'https://tracking.ghn.dev/?order_code={code}', true),
    (2, 'GHTK', 'Giao Hàng Tiết Kiệm', 'https://cdn.haitrieu.com/wp-content/uploads/2022/05/Logo-GHTK-Green.png', true, true, true, 'https://i.ghtk.vn/{code}', true),
    (3, 'AHAMOVE', 'Ahamove On-Demand', 'https://cdn.haitrieu.com/wp-content/uploads/2022/05/Logo-Ahamove-Orange.png', true, false, true, 'https://app.ahamove.com/tracking/{code}', true),
    (4, 'GOSHIP', 'GoShip Multi-Carrier Aggregator', 'https://goship.io/images/logo.png', true, true, true, 'https://goship.io/tracking?code={code}', true)
ON CONFLICT (code) DO NOTHING;

-- Seed Default Services
INSERT INTO ecommerce_shipment.shipping_provider_service (id, provider_id, service_code, service_name, max_weight_kg, is_active)
VALUES
    (1, 1, 'GHN_STANDARD', 'GHN Chuẩn / Giao Tiết Kiệm (service_type_id: 5)', 20.00, true),
    (2, 1, 'GHN_EXPRESS', 'GHN Hỏa Tốc / Giao Nhanh (service_type_id: 2)', 10.00, true),
    (3, 2, 'GHTK_STANDARD', 'GHTK Tiết Kiệm', 20.00, true),
    (4, 2, 'GHTK_FAST', 'GHTK Bay Nhanh', 5.00, true),
    (5, 3, 'AHAMOVE_BIKE', 'Ahamove Giao Xe Máy (SGN-BIKE)', 30.00, true),
    (6, 3, 'AHAMOVE_TRUCK', 'Ahamove Giao Xe Tải (SGN-TRUCK500)', 500.00, true),
    (7, 4, 'GOSHIP_ECONOMY', 'GoShip Tiết Kiệm Đa Đơn Vị', 50.00, true)
ON CONFLICT (provider_id, service_code) DO NOTHING;

-- Seed Default Configs with realistic extra_settings (GHN, GHTK, Ahamove, GoShip)
INSERT INTO ecommerce_shipment.shipping_provider_config (
    id, provider_id, config_name, environment, api_base_url, token, api_key,
    provider_shop_id, provider_account_name,
    default_pickup_name, default_pickup_phone, default_pickup_address, default_pickup_ward, default_pickup_district, default_pickup_province,
    default_service_id, default_payer, default_insurance_enabled, default_weight_gram,
    extra_settings, is_default, is_active
)
VALUES
    (1, 1, 'GHN Kho Mặc Định', 'SANDBOX', 'https://dev-online-gateway.ghn.vn/shiip/public-api', 'sandbox-token-ghn-12345', 'ghn-api-key',
     '123456', 'Shobbe Official Store',
     'Kho Tổng Shobbe', '0987654321', '123 Nguyễn Thị Thập', 'Tân Phú', 'Quận 7', 'Hồ Chí Minh',
     2, 'SHOP', true, 500,
     '{"shop_id": 123456, "client_id": "ghn-client-01", "note_code": "CHOTHUHANG", "from_district_id": 1452, "from_ward_code": "21012", "default_service_type_id": 2, "default_payment_type_id": 1, "insurance_enabled": true}'::jsonb,
     true, true),
    (2, 2, 'GHTK Kho Mặc Định', 'SANDBOX', 'https://services.giaohangtietkiem.vn', 'sandbox-token-ghtk-67890', 'ghtk-api-key',
     'S1234567', 'Shobbe Official Store',
     'Kho Tổng Shobbe', '0987654321', '123 Nguyễn Thị Thập', 'Tân Phú', 'Quận 7', 'Hồ Chí Minh',
     3, 'SHOP', false, 500,
     '{"partner_code": "S1234567", "pick_address_id": "p_addr_01", "default_deliver_option": "xteam", "is_freeship": 1, "use_return_address": false}'::jsonb,
     false, true),
    (3, 3, 'Ahamove Kho Mặc Định', 'SANDBOX', 'https://apandora.ahamove.com/v1', 'sandbox-token-ahamove', 'ahamove-key',
     '0987654321', 'Shobbe Instant Delivery',
     'Kho Tổng Shobbe', '0987654321', '123 Nguyễn Thị Thập', 'Tân Phú', 'Quận 7', 'Hồ Chí Minh',
     5, 'SHOP', false, 1000,
     '{"mobile": "0987654321", "user_id": "AHM-SHOBBE-01", "request_source": "THIRD_PARTY", "app_id": "com.vn.shobbe", "payment_method": "CASH"}'::jsonb,
     false, true),
    (4, 4, 'GoShip Kho Mặc Định', 'SANDBOX', 'https://sandbox.goship.io/api/v2', 'sandbox-token-goship', 'goship-key',
     'GS-PARTNER-01', 'Shobbe Multi Carrier',
     'Kho Tổng Shobbe', '0987654321', '123 Nguyễn Thị Thập', 'Tân Phú', 'Quận 7', 'Hồ Chí Minh',
     7, 'SHOP', true, 500,
     '{"partner_id": "GS-PARTNER-01", "partner_key": "goship_secret_key", "warehouse_code": "WH-HCM-01", "default_service_code": "GOSHIP_ECONOMY"}'::jsonb,
     false, true)
ON CONFLICT (id) DO NOTHING;

-- Seed Sample Shipping Provinces
INSERT INTO ecommerce_shipment.shipping_province (id, provider_id, external_code, province_name, standard_code)
VALUES
    (1, 1, '201', 'Hồ Chí Minh', '79'),
    (2, 1, '202', 'Hà Nội', '01'),
    (3, 1, '203', 'Đà Nẵng', '48'),
    (4, 2, 'HCM', 'Hồ Chí Minh', '79'),
    (5, 2, 'HN', 'Hà Nội', '01'),
    (6, 2, 'DNG', 'Đà Nẵng', '48')
ON CONFLICT (provider_id, external_code) DO NOTHING;

-- Seed Sample Shipping Districts
INSERT INTO ecommerce_shipment.shipping_district (id, provider_id, province_ref_id, external_code, district_name, standard_code)
VALUES
    (1, 1, 1, '1452', 'Quận 7', '778'),
    (2, 1, 1, '1442', 'Quận 1', '760'),
    (3, 1, 2, '1542', 'Quận Ba Đình', '001'),
    (4, 2, 4, 'Q7', 'Quận 7', '778'),
    (5, 2, 4, 'Q1', 'Quận 1', '760')
ON CONFLICT (provider_id, external_code) DO NOTHING;

-- Seed Sample Shipping Wards
INSERT INTO ecommerce_shipment.shipping_ward (id, provider_id, district_ref_id, external_code, ward_name, standard_code)
VALUES
    (1, 1, 1, '21012', 'Phường Tân Phú', '27484'),
    (2, 1, 1, '21011', 'Phường Tân Phong', '27487'),
    (3, 1, 2, '20101', 'Phường Bến Nghé', '26734'),
    (4, 2, 4, 'W_TPHU', 'Phường Tân Phú', '27484'),
    (5, 2, 4, 'W_TPHONG', 'Phường Tân Phong', '27487')
ON CONFLICT (provider_id, external_code) DO NOTHING;

-- Seed Default Rates
INSERT INTO ecommerce_shipment.shipping_rate (
    provider_id, service_id, from_province, to_province, weight_from_gram, weight_to_gram, base_fee, fee_per_extra_kg, cod_fee_percent, is_active
)
VALUES
    (1, 1, 'Hồ Chí Minh', 'Hồ Chí Minh', 0, 2000, 22000, 5000, 0, true),
    (1, 1, 'Hồ Chí Minh', 'Hà Nội', 0, 2000, 35000, 10000, 0, true),
    (2, 3, 'Hồ Chí Minh', 'Hồ Chí Minh', 0, 2000, 20000, 4500, 0, true),
    (2, 3, 'Hồ Chí Minh', 'Hà Nội', 0, 2000, 32000, 9000, 0, true)
ON CONFLICT DO NOTHING;
