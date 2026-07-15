-- ============================================================
-- V11: Reusable Email Module - Production Grade
-- Polymorphic owner: QUOTATION, INVOICE, DELIVERY_CHALLAN, ...
-- ============================================================

CREATE TABLE IF NOT EXISTS email_log (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),

    company_id UUID,
    company_code VARCHAR(20),

    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,

    from_address VARCHAR(255) NOT NULL,
    to_addresses TEXT NOT NULL,
    cc_addresses TEXT,
    bcc_addresses TEXT,

    subject VARCHAR(1000) NOT NULL,
    body_text TEXT,
    body_html TEXT,
    charset VARCHAR(20) DEFAULT 'UTF-8',

    has_attachments BOOLEAN NOT NULL DEFAULT FALSE,
    attachment_count INTEGER NOT NULL DEFAULT 0,
    attachment_total_size_bytes BIGINT NOT NULL DEFAULT 0,
    attachment_summary VARCHAR(500),

    mail_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_type VARCHAR(20) NOT NULL DEFAULT 'SMTP',
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',

    smtp_message_id VARCHAR(255),
    smtp_response_code VARCHAR(10),
    smtp_response_message TEXT,

    queued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,

    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP,

    failure_reason TEXT,
    failure_stack_trace TEXT,

    is_test BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_mail_status CHECK (mail_status IN
('PENDING','PROCESSING','SENT','DELIVERED','FAILED','BOUNCED','REJECTED')),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT'))
    );

CREATE TABLE IF NOT EXISTS email_recipient (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    email_log_id UUID NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    recipient_type VARCHAR(10) NOT NULL,

    delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    bounced_at TIMESTAMP,
    bounce_reason TEXT,

    CONSTRAINT fk_recipient_log FOREIGN KEY (email_log_id) REFERENCES email_log(id) ON DELETE CASCADE,
    CONSTRAINT chk_recipient_type CHECK (recipient_type IN ('TO','CC','BCC'))
    );

CREATE TABLE IF NOT EXISTS email_attachment_log (
                                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    email_log_id UUID NOT NULL,
    filename VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1000),
    inline BOOLEAN NOT NULL DEFAULT FALSE,
    content_id VARCHAR(255),

    CONSTRAINT fk_attachment_log FOREIGN KEY (email_log_id) REFERENCES email_log(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_email_entity ON email_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_email_owner ON email_log(entity_id);
CREATE INDEX IF NOT EXISTS idx_email_company ON email_log(company_id);
CREATE INDEX IF NOT EXISTS idx_email_status ON email_log(mail_status);
CREATE INDEX IF NOT EXISTS idx_email_pending ON email_log(mail_status, queued_at DESC) WHERE mail_status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_email_failed ON email_log(mail_status, retry_count) WHERE mail_status = 'FAILED';
CREATE INDEX IF NOT EXISTS idx_email_priority ON email_log(priority, queued_at);
CREATE INDEX IF NOT EXISTS idx_email_message_id ON email_log(smtp_message_id);

CREATE INDEX IF NOT EXISTS idx_email_recipient_log ON email_recipient(email_log_id);
CREATE INDEX IF NOT EXISTS idx_email_recipient_address ON email_recipient(recipient_address);
CREATE INDEX IF NOT EXISTS idx_email_recipient_bounced ON email_recipient(bounced_at) WHERE bounced_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_attachment_log ON email_attachment_log(email_log_id);

-- V11__seed_generic_rod_specifications.sql

-- ============================================================================
-- SECTION 1: CREATE NEW NORMALIZED TABLES FOR ROD SPECIFICATIONS AND MAPPINGS
-- ============================================================================

CREATE TABLE IF NOT EXISTS generic_rod_specifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    diameter NUMERIC(19, 4) NOT NULL,
    length NUMERIC(19, 4) NOT NULL,
    coolant_hole_type VARCHAR(100) NOT NULL, -- 'NONE', 'CENTRAL', 'MULTI_HOLE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_generic_rod_spec UNIQUE (diameter, length, coolant_hole_type)
);

CREATE TABLE IF NOT EXISTS rod_variant_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id UUID NOT NULL REFERENCES inventory_items(id),
    specification_id UUID NOT NULL REFERENCES generic_rod_specifications(id),
    material_grade_id UUID NOT NULL REFERENCES inventory_material_grades(id),
    price NUMERIC(19, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_rod_variant_mapping UNIQUE (item_id, specification_id, material_grade_id)
);


-- ============================================================================
-- SECTION 2: SEED MATERIAL GRADES
-- ============================================================================

INSERT INTO inventory_material_grades (name, code, description, created_at, created_by)
VALUES
('K40UF / H10F', 'K40UF_H10F', 'K40UF / H10F carbide grade', CURRENT_TIMESTAMP, 'SYSTEM'),
('AM70 / DM80', 'AM70_DM80', 'AM70 / DM80 carbide grade', CURRENT_TIMESTAMP, 'SYSTEM'),
('PN90', 'PN90', 'PN90 carbide grade', CURRENT_TIMESTAMP, 'SYSTEM'),
('GP10 / K10F', 'GP10_K10F', 'GP10 / K10F carbide grade', CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (code) DO NOTHING;


-- ============================================================================
-- SECTION 3: SEED INVENTORY CATEGORY AND ITEMS
-- ============================================================================

-- Ensure the Raw Material category exists
INSERT INTO inventory_categories (name, code, description, created_at, created_by)
VALUES ('Raw Material', 'RAW_MATERIAL', 'Raw Material Category for Carbide Rods', CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Seed Standard Rod Items
INSERT INTO inventory_items (name, sku, category_id, active, created_at, created_by)
VALUES
('3.2 X 330 MM UG CARBIDE ROD', 'CR-3.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('4.2 X 330 MM UG CARBIDE ROD', 'CR-4.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('5.2 X 330 MM UG CARBIDE ROD', 'CR-5.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('6.2 X 330 MM UG CARBIDE ROD', 'CR-6.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('8.2 X 330 MM UG CARBIDE ROD', 'CR-8.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('10.2 X 330 MM UG CARBIDE ROD', 'CR-10.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('12.2 X 330 MM UG CARBIDE ROD', 'CR-12.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('14.2 X 330 MM UG CARBIDE ROD', 'CR-14.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('16.2 X 330 MM UG CARBIDE ROD', 'CR-16.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('18.2 X 330 MM UG CARBIDE ROD', 'CR-18.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('20.2 X 330 MM UG CARBIDE ROD', 'CR-20.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('22.2 X 330 MM UG CARBIDE ROD', 'CR-22.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('25.2 X 330 MM UG CARBIDE ROD', 'CR-25.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('32.2 X 330 MM UG CARBIDE ROD', 'CR-32.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (sku) DO NOTHING;

-- Seed Coolant Hole Rod Items
INSERT INTO inventory_items (name, sku, category_id, active, created_at, created_by)
VALUES
('6.2 X 330 MM COOLANT HOLE ROD', 'CH-6.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('8.2 X 330 MM COOLANT HOLE ROD', 'CH-8.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('10.2 X 330 MM COOLANT HOLE ROD', 'CH-10.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('12.2 X 330 MM COOLANT HOLE ROD', 'CH-12.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('14.2 X 330 MM COOLANT HOLE ROD', 'CH-14.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('16.2 X 330 MM COOLANT HOLE ROD', 'CH-16.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('18.2 X 330 MM COOLANT HOLE ROD', 'CH-18.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('20.2 X 330 MM COOLANT HOLE ROD', 'CH-20.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('22.2 X 330 MM COOLANT HOLE ROD', 'CH-22.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('25.2 X 330 MM COOLANT HOLE ROD', 'CH-25.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
('32.2 X 330 MM COOLANT HOLE ROD', 'CH-32.2-330', (SELECT id FROM inventory_categories WHERE code = 'RAW_MATERIAL' LIMIT 1), TRUE, CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (sku) DO NOTHING;


-- ============================================================================
-- SECTION 4: SEED GENERIC ROD SPECIFICATIONS
-- ============================================================================

INSERT INTO generic_rod_specifications (diameter, length, coolant_hole_type)
VALUES
-- Standard Rod Specifications
(3.2, 330.0000, 'NONE'),
(4.2, 330.0000, 'NONE'),
(5.2, 330.0000, 'NONE'),
(6.2, 330.0000, 'NONE'),
(8.2, 330.0000, 'NONE'),
(10.2, 330.0000, 'NONE'),
(12.2, 330.0000, 'NONE'),
(14.2, 330.0000, 'NONE'),
(16.2, 330.0000, 'NONE'),
(18.2, 330.0000, 'NONE'),
(20.2, 330.0000, 'NONE'),
(22.2, 330.0000, 'NONE'),
(25.2, 330.0000, 'NONE'),
(32.2, 330.0000, 'NONE'),

-- Coolant Hole Central / Parallel Specifications
(6.2, 330.0000, 'CENTRAL'),
(8.2, 330.0000, 'CENTRAL'),
(10.2, 330.0000, 'CENTRAL'),
(12.2, 330.0000, 'CENTRAL'),
(14.2, 330.0000, 'CENTRAL'),
(16.2, 330.0000, 'CENTRAL'),
(18.2, 330.0000, 'CENTRAL'),
(20.2, 330.0000, 'CENTRAL'),
(22.2, 330.0000, 'CENTRAL'),
(25.2, 330.0000, 'CENTRAL'),
(32.2, 330.0000, 'CENTRAL'),

-- Coolant Hole 2 Hole / 3 Hole / 30 Degree / 40 Degree Specifications
(6.2, 330.0000, 'MULTI_HOLE'),
(8.2, 330.0000, 'MULTI_HOLE'),
(10.2, 330.0000, 'MULTI_HOLE'),
(12.2, 330.0000, 'MULTI_HOLE'),
(14.2, 330.0000, 'MULTI_HOLE'),
(16.2, 330.0000, 'MULTI_HOLE'),
(18.2, 330.0000, 'MULTI_HOLE'),
(20.2, 330.0000, 'MULTI_HOLE'),
(22.2, 330.0000, 'MULTI_HOLE'),
(25.2, 330.0000, 'MULTI_HOLE'),
(32.2, 330.0000, 'MULTI_HOLE')
ON CONFLICT (diameter, length, coolant_hole_type) DO NOTHING;


-- ============================================================================
-- SECTION 5: SEED ROD VARIANT MAPPING (NORMALIZED RATE CHART DATA)
-- ============================================================================

DO $$
BEGIN
    -- Temporary function block to insert normalized data without hardcoding UUIDs
    INSERT INTO rod_variant_mapping (item_id, specification_id, material_grade_id, price)
    SELECT
        i.id AS item_id,
        s.id AS specification_id,
        g.id AS material_grade_id,
        pricing.price
    FROM (
        VALUES
        -- Standard Rods - K40UF_H10F
        ('CR-3.2-330', 3.2, 330.00, 'NONE', 'K40UF_H10F', 150.00),
        ('CR-4.2-330', 4.2, 330.00, 'NONE', 'K40UF_H10F', 200.00),
        ('CR-5.2-330', 5.2, 330.00, 'NONE', 'K40UF_H10F', 250.00),
        ('CR-6.2-330', 6.2, 330.00, 'NONE', 'K40UF_H10F', 300.00),
        ('CR-8.2-330', 8.2, 330.00, 'NONE', 'K40UF_H10F', 450.00),
        ('CR-10.2-330', 10.2, 330.00, 'NONE', 'K40UF_H10F', 700.00),
        ('CR-12.2-330', 12.2, 330.00, 'NONE', 'K40UF_H10F', 1000.00),
        ('CR-14.2-330', 14.2, 330.00, 'NONE', 'K40UF_H10F', 1350.00),
        ('CR-16.2-330', 16.2, 330.00, 'NONE', 'K40UF_H10F', 1750.00),
        ('CR-18.2-330', 18.2, 330.00, 'NONE', 'K40UF_H10F', 2200.00),
        ('CR-20.2-330', 20.2, 330.00, 'NONE', 'K40UF_H10F', 2700.00),
        ('CR-22.2-330', 22.2, 330.00, 'NONE', 'K40UF_H10F', 3250.00),
        ('CR-25.2-330', 25.2, 330.00, 'NONE', 'K40UF_H10F', 4200.00),
        ('CR-32.2-330', 32.2, 330.00, 'NONE', 'K40UF_H10F', 6800.00),

        -- Standard Rods - AM70_DM80
        ('CR-3.2-330', 3.2, 330.00, 'NONE', 'AM70_DM80', 160.00),
        ('CR-4.2-330', 4.2, 330.00, 'NONE', 'AM70_DM80', 210.00),
        ('CR-5.2-330', 5.2, 330.00, 'NONE', 'AM70_DM80', 260.00),
        ('CR-6.2-330', 6.2, 330.00, 'NONE', 'AM70_DM80', 320.00),
        ('CR-8.2-330', 8.2, 330.00, 'NONE', 'AM70_DM80', 480.00),
        ('CR-10.2-330', 10.2, 330.00, 'NONE', 'AM70_DM80', 750.00),
        ('CR-12.2-330', 12.2, 330.00, 'NONE', 'AM70_DM80', 1080.00),
        ('CR-14.2-330', 14.2, 330.00, 'NONE', 'AM70_DM80', 1450.00),
        ('CR-16.2-330', 16.2, 330.00, 'NONE', 'AM70_DM80', 1880.00),
        ('CR-18.2-330', 18.2, 330.00, 'NONE', 'AM70_DM80', 2360.00),
        ('CR-20.2-330', 20.2, 330.00, 'NONE', 'AM70_DM80', 2900.00),
        ('CR-22.2-330', 22.2, 330.00, 'NONE', 'AM70_DM80', 3500.00),
        ('CR-25.2-330', 25.2, 330.00, 'NONE', 'AM70_DM80', 4500.00),
        ('CR-32.2-330', 32.2, 330.00, 'NONE', 'AM70_DM80', 7300.00),

        -- Standard Rods - PN90
        ('CR-3.2-330', 3.2, 330.00, 'NONE', 'PN90', 170.00),
        ('CR-4.2-330', 4.2, 330.00, 'NONE', 'PN90', 220.00),
        ('CR-5.2-330', 5.2, 330.00, 'NONE', 'PN90', 270.00),
        ('CR-6.2-330', 6.2, 330.00, 'NONE', 'PN90', 330.00),
        ('CR-8.2-330', 8.2, 330.00, 'NONE', 'PN90', 500.00),
        ('CR-10.2-330', 10.2, 330.00, 'NONE', 'PN90', 780.00),
        ('CR-12.2-330', 12.2, 330.00, 'NONE', 'PN90', 1120.00),
        ('CR-14.2-330', 14.2, 330.00, 'NONE', 'PN90', 1500.00),
        ('CR-16.2-330', 16.2, 330.00, 'NONE', 'PN90', 1950.00),
        ('CR-18.2-330', 18.2, 330.00, 'NONE', 'PN90', 2450.00),
        ('CR-20.2-330', 20.2, 330.00, 'NONE', 'PN90', 3000.00),
        ('CR-22.2-330', 22.2, 330.00, 'NONE', 'PN90', 3620.00),
        ('CR-25.2-330', 25.2, 330.00, 'NONE', 'PN90', 4660.00),
        ('CR-32.2-330', 32.2, 330.00, 'NONE', 'PN90', 7550.00),

        -- Standard Rods - GP10_K10F
        ('CR-3.2-330', 3.2, 330.00, 'NONE', 'GP10_K10F', 140.00),
        ('CR-4.2-330', 4.2, 330.00, 'NONE', 'GP10_K10F', 190.00),
        ('CR-5.2-330', 5.2, 330.00, 'NONE', 'GP10_K10F', 240.00),
        ('CR-6.2-330', 6.2, 330.00, 'NONE', 'GP10_K10F', 280.00),
        ('CR-8.2-330', 8.2, 330.00, 'NONE', 'GP10_K10F', 420.00),
        ('CR-10.2-330', 10.2, 330.00, 'NONE', 'GP10_K10F', 650.00),
        ('CR-12.2-330', 12.2, 330.00, 'NONE', 'GP10_K10F', 930.00),
        ('CR-14.2-330', 14.2, 330.00, 'NONE', 'GP10_K10F', 1250.00),
        ('CR-16.2-330', 16.2, 330.00, 'NONE', 'GP10_K10F', 1620.00),
        ('CR-18.2-330', 18.2, 330.00, 'NONE', 'GP10_K10F', 2040.00),
        ('CR-20.2-330', 20.2, 330.00, 'NONE', 'GP10_K10F', 2500.00),
        ('CR-22.2-330', 22.2, 330.00, 'NONE', 'GP10_K10F', 3020.00),
        ('CR-25.2-330', 25.2, 330.00, 'NONE', 'GP10_K10F', 3900.00),
        ('CR-32.2-330', 32.2, 330.00, 'NONE', 'GP10_K10F', 6300.00),

        -- Coolant Hole Rods - Central / Parallel Hole (K40UF_H10F)
        ('CH-6.2-330', 6.2, 330.00, 'CENTRAL', 'K40UF_H10F', 450.00),
        ('CH-8.2-330', 8.2, 330.00, 'CENTRAL', 'K40UF_H10F', 675.00),
        ('CH-10.2-330', 10.2, 330.00, 'CENTRAL', 'K40UF_H10F', 1050.00),
        ('CH-12.2-330', 12.2, 330.00, 'CENTRAL', 'K40UF_H10F', 1500.00),
        ('CH-14.2-330', 14.2, 330.00, 'CENTRAL', 'K40UF_H10F', 2025.00),
        ('CH-16.2-330', 16.2, 330.00, 'CENTRAL', 'K40UF_H10F', 2625.00),
        ('CH-18.2-330', 18.2, 330.00, 'CENTRAL', 'K40UF_H10F', 3300.00),
        ('CH-20.2-330', 20.2, 330.00, 'CENTRAL', 'K40UF_H10F', 4050.00),
        ('CH-22.2-330', 22.2, 330.00, 'CENTRAL', 'K40UF_H10F', 4875.00),
        ('CH-25.2-330', 25.2, 330.00, 'CENTRAL', 'K40UF_H10F', 6300.00),
        ('CH-32.2-330', 32.2, 330.00, 'CENTRAL', 'K40UF_H10F', 10200.00),

        -- Coolant Hole Rods - 2 Hole / 3 Hole / 30 Degree / 40 Degree (K40UF_H10F)
        ('CH-6.2-330', 6.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 600.00),
        ('CH-8.2-330', 8.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 900.00),
        ('CH-10.2-330', 10.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 1400.00),
        ('CH-12.2-330', 12.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 2000.00),
        ('CH-14.2-330', 14.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 2700.00),
        ('CH-16.2-330', 16.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 3500.00),
        ('CH-18.2-330', 18.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 4400.00),
        ('CH-20.2-330', 20.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 5400.00),
        ('CH-22.2-330', 22.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 6500.00),
        ('CH-25.2-330', 25.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 8400.00),
        ('CH-32.2-330', 32.2, 330.00, 'MULTI_HOLE', 'K40UF_H10F', 13600.00)
    ) AS pricing(sku, diameter, len, hole_type, grade_code, price)
    JOIN inventory_items i ON i.sku = pricing.sku
    JOIN generic_rod_specifications s ON s.diameter = pricing.diameter AND s.length = pricing.len AND s.coolant_hole_type = pricing.hole_type
    JOIN inventory_material_grades g ON g.code = pricing.grade_code
    ON CONFLICT (item_id, specification_id, material_grade_id) DO UPDATE
    SET price = EXCLUDED.price,
        updated_at = CURRENT_TIMESTAMP;
END $$;


-- ============================================================================
-- SECTION 6: SEED SEPARATE HYPERION RATE CHART TABLES FOR EXCEL IMPORT
-- ============================================================================

INSERT INTO hyperion_rod_net_price (id, item, k40uf_h10f, am70_dm80, pn90, gp10_k10f, active, created_at, created_by)
VALUES
(gen_random_uuid(), '3.2 X 330 MM UG CARBIDE ROD', 150.00, 160.00, 170.00, 140.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '4.2 X 330 MM UG CARBIDE ROD', 200.00, 210.00, 220.00, 190.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '5.2 X 330 MM UG CARBIDE ROD', 250.00, 260.00, 270.00, 240.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '6.2 X 330 MM UG CARBIDE ROD', 300.00, 320.00, 330.00, 280.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '8.2 X 330 MM UG CARBIDE ROD', 450.00, 480.00, 500.00, 420.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '10.2 X 330 MM UG CARBIDE ROD', 700.00, 750.00, 780.00, 650.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '12.2 X 330 MM UG CARBIDE ROD', 1000.00, 1080.00, 1120.00, 930.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '14.2 X 330 MM UG CARBIDE ROD', 1350.00, 1450.00, 1500.00, 1250.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '16.2 X 330 MM UG CARBIDE ROD', 1750.00, 1880.00, 1950.00, 1620.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '18.2 X 330 MM UG CARBIDE ROD', 2200.00, 2360.00, 2450.00, 2040.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '20.2 X 330 MM UG CARBIDE ROD', 2700.00, 2900.00, 3000.00, 2500.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '22.2 X 330 MM UG CARBIDE ROD', 3250.00, 3500.00, 3620.00, 3020.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '25.2 X 330 MM UG CARBIDE ROD', 4200.00, 4500.00, 4660.00, 3900.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '32.2 X 330 MM UG CARBIDE ROD', 6800.00, 7300.00, 7550.00, 6300.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (item) DO UPDATE
SET k40uf_h10f = EXCLUDED.k40uf_h10f,
    am70_dm80 = EXCLUDED.am70_dm80,
    pn90 = EXCLUDED.pn90,
    gp10_k10f = EXCLUDED.gp10_k10f,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO hyperion_coolant_hole_rod_price (id, category, item, price, active, created_at, created_by)
VALUES
-- Central / Parallel Hole
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '6.2 X 330 MM COOLANT HOLE ROD', 450.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '8.2 X 330 MM COOLANT HOLE ROD', 675.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '10.2 X 330 MM COOLANT HOLE ROD', 1050.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '12.2 X 330 MM COOLANT HOLE ROD', 1500.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '14.2 X 330 MM COOLANT HOLE ROD', 2025.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '16.2 X 330 MM COOLANT HOLE ROD', 2625.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '18.2 X 330 MM COOLANT HOLE ROD', 3300.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '20.2 X 330 MM COOLANT HOLE ROD', 4050.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '22.2 X 330 MM COOLANT HOLE ROD', 4875.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '25.2 X 330 MM COOLANT HOLE ROD', 6300.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'Central / Parallel Hole (Grade K40, Length 330 mm)', '32.2 X 330 MM COOLANT HOLE ROD', 10200.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),

-- 2 Hole / 3 Hole / 30 Degree / 40 Degree
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '6.2 X 330 MM COOLANT HOLE ROD', 600.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '8.2 X 330 MM COOLANT HOLE ROD', 900.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '10.2 X 330 MM COOLANT HOLE ROD', 1400.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '12.2 X 330 MM COOLANT HOLE ROD', 2000.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '14.2 X 330 MM COOLANT HOLE ROD', 2700.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '16.2 X 330 MM COOLANT HOLE ROD', 3500.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '18.2 X 330 MM COOLANT HOLE ROD', 4400.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '20.2 X 330 MM COOLANT HOLE ROD', 5400.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '22.2 X 330 MM COOLANT HOLE ROD', 6500.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '25.2 X 330 MM COOLANT HOLE ROD', 8400.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), '2 Hole / 3 Hole / 30 Degree / 40 Degree (Grade K40, Length 330 mm)', '32.2 X 330 MM COOLANT HOLE ROD', 13600.00, TRUE, CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (category, item) DO UPDATE
SET price = EXCLUDED.price,
    updated_at = CURRENT_TIMESTAMP;
