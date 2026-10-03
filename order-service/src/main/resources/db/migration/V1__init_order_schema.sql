-- Schema initialization for Order Service
CREATE SCHEMA IF NOT EXISTS ecommerce_order;

CREATE TABLE IF NOT EXISTS ecommerce_order.orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(30) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    customer_name VARCHAR(100),
    customer_email VARCHAR(150),
    customer_phone VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(30) DEFAULT 'PENDING',
    total_amount NUMERIC(15, 3) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 3) DEFAULT 0,
    shipping_fee NUMERIC(15, 3) DEFAULT 0,
    final_amount NUMERIC(15, 3) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(15, 3) DEFAULT 0,
    refunded_amount NUMERIC(15, 3) DEFAULT 0,
    note TEXT,
    shipping_address_id BIGINT,
    shipping_full_name VARCHAR(100),
    shipping_phone VARCHAR(20),
    shipping_address_line VARCHAR(255),
    shipping_ward VARCHAR(100),
    shipping_district VARCHAR(100),
    shipping_province VARCHAR(100),
    shipping_country VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ecommerce_order.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES ecommerce_order.orders(id) ON DELETE CASCADE,
    product_id BIGINT,
    variant_id BIGINT,
    sku VARCHAR(100),
    product_name VARCHAR(255) NOT NULL,
    variant_attributes JSONB,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 3) NOT NULL,
    subtotal NUMERIC(15, 3) NOT NULL
);

CREATE TABLE IF NOT EXISTS ecommerce_order.order_histories (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES ecommerce_order.orders(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    note TEXT,
    created_by BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ecommerce_order.outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(200) NOT NULL,
    kafka_key VARCHAR(200),
    payload_class VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 5,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON ecommerce_order.orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON ecommerce_order.orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON ecommerce_order.orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON ecommerce_order.order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_histories_order_id ON ecommerce_order.order_histories(order_id);
CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON ecommerce_order.outbox_events(status, created_at);
