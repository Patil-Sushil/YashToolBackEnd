-- ============================================================
-- V17: Work Order Management System
-- ============================================================

CREATE TABLE IF NOT EXISTS work_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    work_order_no VARCHAR(30) NOT NULL UNIQUE,
    quotation_id UUID NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    customer_id UUID NOT NULL,
    customer_company_name VARCHAR(255),
    customer_contact_person VARCHAR(255),
    customer_email VARCHAR(255),
    customer_mobile VARCHAR(20),
    remarks TEXT,
    planned_start_date DATE,
    planned_end_date DATE,
    actual_start_date TIMESTAMP,
    actual_end_date TIMESTAMP,

    CONSTRAINT fk_work_order_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_work_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_work_order_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT chk_work_order_status CHECK (status IN ('CREATED', 'IN_PROGRESS', 'PRODUCTION_COMPLETED', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS work_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    work_order_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    quotation_item_id UUID,
    order_type VARCHAR(20) NOT NULL,
    tool_name VARCHAR(200) NOT NULL,
    item_name VARCHAR(200),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    trial BOOLEAN NOT NULL DEFAULT FALSE,
    item_remarks TEXT,
    drawing_reference VARCHAR(500),
    material_type VARCHAR(30),
    material_grade VARCHAR(30),
    coating_required BOOLEAN NOT NULL DEFAULT FALSE,
    coating_type VARCHAR(30),
    resharpening_type VARCHAR(20),
    diameter DOUBLE PRECISION,
    flute_length DOUBLE PRECISION,
    shank_diameter DOUBLE PRECISION,
    overall_length DOUBLE PRECISION,
    technical_notes TEXT,
    damage_level VARCHAR(50),
    special_geometry BOOLEAN NOT NULL DEFAULT FALSE,
    special_profile BOOLEAN NOT NULL DEFAULT FALSE,
    express_delivery BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_wo_item_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_wo_item_quotation_item FOREIGN KEY (quotation_item_id) REFERENCES quotation_items(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_work_order_no ON work_orders(work_order_no);
CREATE INDEX IF NOT EXISTS idx_work_order_status ON work_orders(status);
CREATE INDEX IF NOT EXISTS idx_work_order_company ON work_orders(company_id);
CREATE INDEX IF NOT EXISTS idx_work_order_quotation ON work_orders(quotation_id);
CREATE INDEX IF NOT EXISTS idx_woitem_work_order ON work_order_items(work_order_id);
CREATE INDEX IF NOT EXISTS idx_woitem_quotation_item ON work_order_items(quotation_item_id);
