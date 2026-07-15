-- ============================================================
-- V15: Create tool_service_rate_masters table
-- Target DB: PostgreSQL
-- ============================================================

CREATE TABLE IF NOT EXISTS tool_service_rate_masters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    service_code VARCHAR(50) NOT NULL,
    service_type VARCHAR(30) NOT NULL,
    tool_type VARCHAR(100) NOT NULL,
    tool_material VARCHAR(50) NOT NULL,
    diameter_from DOUBLE PRECISION NOT NULL,
    diameter_to DOUBLE PRECISION NOT NULL,
    no_of_flutes INTEGER,
    profile_type VARCHAR(100),
    base_rate NUMERIC(12, 2) NOT NULL,
    minor_damage_charge NUMERIC(12, 2),
    medium_damage_charge NUMERIC(12, 2),
    major_damage_charge NUMERIC(12, 2),
    coating_tin NUMERIC(12, 2),
    coating_tialn NUMERIC(12, 2),
    coating_alcrn NUMERIC(12, 2),
    coating_dlc NUMERIC(12, 2),
    special_geometry_charge NUMERIC(12, 2),
    special_profile_charge NUMERIC(12, 2),
    express_delivery_charge NUMERIC(12, 2),
    standard_delivery_days INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_service_rate_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT uk_company_service_code UNIQUE (company_id, service_code)
);

CREATE INDEX IF NOT EXISTS idx_srv_rate_lookup 
ON tool_service_rate_masters (company_id, active, service_type, tool_material, diameter_from, diameter_to);

-- Add service pricing inputs to quotation_items
ALTER TABLE quotation_items ADD COLUMN IF NOT EXISTS damage_level VARCHAR(50);
ALTER TABLE quotation_items ADD COLUMN IF NOT EXISTS special_geometry BOOLEAN DEFAULT FALSE;
ALTER TABLE quotation_items ADD COLUMN IF NOT EXISTS special_profile BOOLEAN DEFAULT FALSE;
ALTER TABLE quotation_items ADD COLUMN IF NOT EXISTS express_delivery BOOLEAN DEFAULT FALSE;

-- V15__vendor_payment.sql
-- PostgreSQL schemas for Vendor Payment & Outstanding Management

-- 1. Alter purchase_invoices to add outstanding tracking fields
ALTER TABLE purchase_invoices
ADD COLUMN IF NOT EXISTS due_date DATE,
ADD COLUMN IF NOT EXISTS total_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
ADD COLUMN IF NOT EXISTS outstanding_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'APPROVED';

-- 2. Backfill existing purchase invoices total_amount and outstanding_amount
UPDATE purchase_invoices pi
SET total_amount = COALESCE((
    SELECT SUM(item.total_amount)
    FROM purchase_invoice_items item
    WHERE item.invoice_id = pi.id
), 0) + pi.freight + pi.other_charges;

UPDATE purchase_invoices
SET outstanding_amount = total_amount - paid_amount;

-- 3. Create Vendor Payment Header Table
CREATE TABLE IF NOT EXISTS purchase_vendor_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_number VARCHAR(100) NOT NULL,
    payment_date DATE NOT NULL,
    vendor_id UUID NOT NULL REFERENCES purchase_vendors(id),
    payment_method VARCHAR(50) NOT NULL, -- CASH, BANK_TRANSFER, UPI, CHEQUE, NEFT, RTGS
    payment_reference_number VARCHAR(100),
    remarks TEXT,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_payment_company_number UNIQUE (company_id, payment_number)
);

-- 4. Create Vendor Payment Items Table
CREATE TABLE IF NOT EXISTS purchase_vendor_payment_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_payment_id UUID NOT NULL REFERENCES purchase_vendor_payments(id) ON DELETE CASCADE,
    purchase_invoice_id UUID NOT NULL REFERENCES purchase_invoices(id),
    invoice_number VARCHAR(100) NOT NULL,
    invoice_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    outstanding_before_payment NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    paid_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    outstanding_after_payment NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
