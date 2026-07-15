-- ============================================================
-- V14: Add email and phone columns to companies table
-- Target DB: PostgreSQL
-- ============================================================

ALTER TABLE companies ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE companies ADD COLUMN IF NOT EXISTS phone VARCHAR(50);

-- Update seeded companies with initial email and phone details
UPDATE companies SET email = 'SWARA.ENTERPRISES5555@GMAIL.COM', phone = '7709228568 / 8149818555' WHERE code = 'SW';
UPDATE companies SET email = 'info@yashtools.com', phone = '02712-234567' WHERE code = 'YT';

-- V14__purchase_module.sql
-- PostgreSQL schemas for Purchase Management Module

-- 1. Payment Terms Master
CREATE TABLE IF NOT EXISTS purchase_payment_terms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    number_of_days INT NOT NULL DEFAULT 0,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_payment_terms_company_code UNIQUE (company_id, code)
);

-- 2. Purchase Type Master
CREATE TABLE IF NOT EXISTS purchase_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_purchase_type_company_code UNIQUE (company_id, code)
);

-- 3. Vendor Master
CREATE TABLE IF NOT EXISTS purchase_vendors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_name VARCHAR(255) NOT NULL,
    gstin VARCHAR(20),
    pan VARCHAR(20),
    contact_details VARCHAR(255),
    address TEXT,
    payment_terms_id UUID REFERENCES purchase_payment_terms(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_vendor_company_name UNIQUE (company_id, vendor_name)
);

-- 4. Purchase Order
CREATE TABLE IF NOT EXISTS purchase_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    po_number VARCHAR(100) NOT NULL,
    po_date DATE NOT NULL,
    vendor_id UUID NOT NULL REFERENCES purchase_vendors(id),
    expected_delivery_date DATE,
    payment_terms_id UUID REFERENCES purchase_payment_terms(id),
    purchase_type_id UUID REFERENCES purchase_types(id),
    status VARCHAR(50) NOT NULL, -- DRAFT, APPROVED, PARTIALLY_RECEIVED, FULLY_RECEIVED, CLOSED, CANCELLED
    remarks TEXT,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_po_company_number UNIQUE (company_id, po_number)
);

-- 5. Purchase Order Item
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES inventory_items(id),
    material_grade_id UUID REFERENCES inventory_material_grades(id),
    ordered_quantity NUMERIC(19, 4) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    rate NUMERIC(19, 4) NOT NULL,
    gst_percentage NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    discount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    line_total NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 6. Goods Receipt Note (GRN)
CREATE TABLE IF NOT EXISTS purchase_goods_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grn_number VARCHAR(100) NOT NULL,
    grn_date DATE NOT NULL,
    vendor_id UUID NOT NULL REFERENCES purchase_vendors(id),
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),
    warehouse VARCHAR(255),
    received_by VARCHAR(255),
    remarks TEXT,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_grn_company_number UNIQUE (company_id, grn_number)
);

-- 7. Goods Receipt Note Item
CREATE TABLE IF NOT EXISTS purchase_goods_receipt_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goods_receipt_id UUID NOT NULL REFERENCES purchase_goods_receipts(id) ON DELETE CASCADE,
    po_item_reference_id UUID NOT NULL REFERENCES purchase_order_items(id),
    item_id UUID NOT NULL REFERENCES inventory_items(id),
    material_grade_id UUID REFERENCES inventory_material_grades(id),
    ordered_quantity NUMERIC(19, 4) NOT NULL,
    previously_received_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    current_received_quantity NUMERIC(19, 4) NOT NULL,
    accepted_quantity NUMERIC(19, 4) NOT NULL,
    rejected_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    pending_quantity NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 8. Purchase Invoice
CREATE TABLE IF NOT EXISTS purchase_invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(100) NOT NULL,
    supplier_invoice_number VARCHAR(100) NOT NULL,
    invoice_date DATE NOT NULL,
    vendor_id UUID NOT NULL REFERENCES purchase_vendors(id),
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),
    gst_details TEXT,
    freight NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    other_charges NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_invoice_company_number UNIQUE (company_id, invoice_number)
);

-- 9. Purchase Invoice GRN Mapping (Many-to-Many join table)
CREATE TABLE IF NOT EXISTS purchase_invoice_grns (
    invoice_id UUID NOT NULL REFERENCES purchase_invoices(id) ON DELETE CASCADE,
    goods_receipt_id UUID NOT NULL REFERENCES purchase_goods_receipts(id) ON DELETE RESTRICT,
    PRIMARY KEY (invoice_id, goods_receipt_id)
);

-- 10. Purchase Invoice Item
CREATE TABLE IF NOT EXISTS purchase_invoice_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES purchase_invoices(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES inventory_items(id),
    material_grade_id UUID REFERENCES inventory_material_grades(id),
    invoice_quantity NUMERIC(19, 4) NOT NULL,
    invoice_rate NUMERIC(19, 4) NOT NULL,
    gst NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    taxable_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    total_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 11. Purchase Return
CREATE TABLE IF NOT EXISTS purchase_returns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_number VARCHAR(100) NOT NULL,
    return_date DATE NOT NULL,
    vendor_id UUID NOT NULL REFERENCES purchase_vendors(id),
    purchase_invoice_id UUID REFERENCES purchase_invoices(id),
    goods_receipt_id UUID NOT NULL REFERENCES purchase_goods_receipts(id),
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),
    remarks TEXT,
    company_id UUID NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_return_company_number UNIQUE (company_id, return_number)
);

-- 12. Purchase Return Item
CREATE TABLE IF NOT EXISTS purchase_return_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_return_id UUID NOT NULL REFERENCES purchase_returns(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES inventory_items(id),
    material_grade_id UUID REFERENCES inventory_material_grades(id),
    quantity NUMERIC(19, 4) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    rate NUMERIC(19, 4) NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
