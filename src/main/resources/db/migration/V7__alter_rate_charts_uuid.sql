-- ============================================================
-- V7: Alter Rate Charts to use UUID and Auditing (Common / Global)
-- PostgreSQL 14+
-- ============================================================

DROP TABLE IF EXISTS hyperion_coolant_hole_rod_price;
DROP TABLE IF EXISTS hyperion_rod_net_price;

CREATE TABLE IF NOT EXISTS hyperion_rod_net_price (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item VARCHAR(255) NOT NULL,
    k40uf_h10f DOUBLE PRECISION NOT NULL,
    am70_dm80 DOUBLE PRECISION NOT NULL,
    pn90 DOUBLE PRECISION NOT NULL,
    gp10_k10f DOUBLE PRECISION NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_hyperion_rod_net_price_item UNIQUE (item)
);

CREATE TABLE IF NOT EXISTS hyperion_coolant_hole_rod_price (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(255) NOT NULL,
    item VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_coolant_hole_category_item UNIQUE (category, item)
);
