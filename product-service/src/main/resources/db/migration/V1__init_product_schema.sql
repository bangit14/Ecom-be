-- =====================================================================
-- PRODUCT SERVICE - POSTGRESQL DATABASE SCHEMA (FLYWAY MIGRATION V1)
-- Quản lý Danh mục, Sản phẩm (đa người bán - store_id), Biến thể, Ảnh, Đánh giá, Yêu thích
-- =====================================================================

-- 0. Khởi tạo Extensions cần thiết
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Tạo schema riêng cho product-service
CREATE SCHEMA IF NOT EXISTS ecommerce_product;
SET search_path TO ecommerce_product, public;

-- ---------------------------------------------------------------------
-- 1. categories
-- Bảng danh mục ngành hàng đa cấp (Category Tree)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id       UUID REFERENCES categories(id) ON DELETE SET NULL,
    name            VARCHAR(150) NOT NULL,
    slug            VARCHAR(160) NOT NULL UNIQUE,
    description     TEXT,
    image_url       VARCHAR(500),
    sort_order      INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);

-- ---------------------------------------------------------------------
-- 2. products
-- Bảng sản phẩm chính, thuộc sở hữu của gian hàng (store_id)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID NOT NULL,                                      -- FK logic tới auth-service (stores.id) / gian hàng người bán
    category_id     UUID REFERENCES categories(id) ON DELETE SET NULL,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(280) NOT NULL UNIQUE,
    description     TEXT,
    brand           VARCHAR(150),
    status          VARCHAR(50) NOT NULL DEFAULT 'DRAFT',               -- DRAFT | ACTIVE | ARCHIVED
    attributes      JSONB,                                              -- Thuộc tính mô tả chung: chất liệu, xuất xứ, bảo hành...
    rating_avg      NUMERIC(3,2) NOT NULL DEFAULT 0,                   -- Denormalized, cập nhật khi có review mới
    rating_count    INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_products_store_id ON products(store_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING GIN (name gin_trgm_ops);

-- ---------------------------------------------------------------------
-- 3. product_variants
-- Bảng biến thể sản phẩm (SKU, Size, Màu, Giá riêng, Tồn kho riêng)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_variants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku                 VARCHAR(100) NOT NULL UNIQUE,
    price               NUMERIC(12,2) NOT NULL,
    compare_at_price    NUMERIC(12,2),                                  -- Giá gốc gạch ngang trước khi giảm
    stock_quantity      INT NOT NULL DEFAULT 0,
    reserved_quantity   INT NOT NULL DEFAULT 0,                          -- Số lượng giữ chỗ cho đơn chưa thanh toán
    options             JSONB NOT NULL DEFAULT '{}',                     -- {"color":"Đen","size":"XL"}
    weight_grams        INT,
    is_default          BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_variants_product_id ON product_variants(product_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_variant_default_per_product
    ON product_variants(product_id) WHERE is_default = TRUE;

-- ---------------------------------------------------------------------
-- 4. product_images
-- Quản lý hình ảnh sản phẩm / biến thể và lưu trữ MinIO / S3
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_images (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id          UUID REFERENCES product_variants(id) ON DELETE CASCADE, -- NULL = ảnh chung của product

    storage_provider    VARCHAR(20) NOT NULL DEFAULT 'minio',            -- 'minio' | 's3' | 'cdn'
    bucket              VARCHAR(100) NOT NULL DEFAULT 'ecommerce-products', -- Tên bucket MinIO
    object_key          VARCHAR(500) NOT NULL,                           -- Đường dẫn file trong bucket (products/uuid/image1.jpg)
    url                 VARCHAR(500),                                    -- Public URL hoặc CDN URL

    mime_type           VARCHAR(50),
    file_size_bytes     INT,
    width               INT,
    height              INT,

    alt_text            VARCHAR(255),
    sort_order          INT NOT NULL DEFAULT 0,
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_images_product_id ON product_images(product_id);
CREATE INDEX IF NOT EXISTS idx_images_variant_id ON product_images(variant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_image_primary_per_product
    ON product_images(product_id) WHERE is_primary = TRUE AND variant_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_image_bucket_key ON product_images(bucket, object_key);

-- ---------------------------------------------------------------------
-- 5. reviews
-- Đánh giá sản phẩm từ người mua hàng (user_id)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id              UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id                 UUID NOT NULL,                               -- FK logic tới auth-service / account người mua
    order_item_id           UUID,                                        -- FK logic tới order-service để xác thực mua hàng
    rating                  SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title                   VARCHAR(200),
    content                 TEXT,
    is_verified_purchase    BOOLEAN NOT NULL DEFAULT FALSE,
    status                  VARCHAR(50) NOT NULL DEFAULT 'PENDING',      -- PENDING | APPROVED | REJECTED
    helpful_count           INT NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_review_per_user_product UNIQUE (product_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews(product_id) WHERE status = 'APPROVED';
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);

-- ---------------------------------------------------------------------
-- 6. wishlists
-- Danh sách sản phẩm yêu thích của người dùng
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS wishlists (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,                                       -- FK logic tới auth-service
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id      UUID REFERENCES product_variants(id) ON DELETE SET NULL, -- Tùy chọn lưu đúng biến thể
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_wishlists_user_id ON wishlists(user_id);
CREATE INDEX IF NOT EXISTS idx_wishlists_product_id ON wishlists(product_id);
