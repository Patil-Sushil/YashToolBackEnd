-- ============================================================
-- V21: Finished Goods, Packing, Logistics, and Invoicing
-- ============================================================

CREATE TABLE IF NOT EXISTS finished_goods_stocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    work_order_item_id UUID NOT NULL UNIQUE,
    quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_fg_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fg_wo_item FOREIGN KEY (work_order_item_id) REFERENCES work_order_items(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS packing_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    work_order_item_id UUID NOT NULL,
    packing_no VARCHAR(50) NOT NULL UNIQUE,
    batch_no VARCHAR(50) NOT NULL,
    package_size VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    container_status VARCHAR(50) NOT NULL DEFAULT 'PACKED',
    remarks TEXT,
    CONSTRAINT fk_packing_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_packing_wo_item FOREIGN KEY (work_order_item_id) REFERENCES work_order_items(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS delivery_challans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    challan_no VARCHAR(30) NOT NULL UNIQUE,
    work_order_id UUID NOT NULL,
    vehicle_no VARCHAR(50),
    driver_name VARCHAR(100),
    driver_contact VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    delivery_date DATE,
    delivery_receipt_by VARCHAR(255),
    delivery_receipt_at TIMESTAMP,
    remarks TEXT,
    CONSTRAINT fk_dc_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dc_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE RESTRICT,
    CONSTRAINT chk_dc_status CHECK (status IN ('DRAFT', 'DISPATCHED', 'DELIVERED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS delivery_challan_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    delivery_challan_id UUID NOT NULL,
    work_order_item_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_dci_challan FOREIGN KEY (delivery_challan_id) REFERENCES delivery_challans(id) ON DELETE CASCADE,
    CONSTRAINT fk_dci_wo_item FOREIGN KEY (work_order_item_id) REFERENCES work_order_items(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS sales_invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    invoice_no VARCHAR(30) NOT NULL UNIQUE,
    work_order_id UUID NOT NULL,
    invoice_date DATE NOT NULL,
    sub_total NUMERIC(19, 4) NOT NULL,
    cgst_rate NUMERIC(5, 2) NOT NULL,
    cgst_amount NUMERIC(19, 4) NOT NULL,
    sgst_rate NUMERIC(5, 2) NOT NULL,
    sgst_amount NUMERIC(19, 4) NOT NULL,
    igst_rate NUMERIC(5, 2) NOT NULL,
    igst_amount NUMERIC(19, 4) NOT NULL,
    total_amount NUMERIC(19, 4) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    remarks TEXT,
    CONSTRAINT fk_invoice_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE RESTRICT,
    CONSTRAINT chk_invoice_status CHECK (status IN ('DRAFT', 'PAID', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS sales_invoice_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    sales_invoice_id UUID NOT NULL,
    work_order_item_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL,
    total_price NUMERIC(19, 4) NOT NULL,
    CONSTRAINT fk_sii_invoice FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_sii_wo_item FOREIGN KEY (work_order_item_id) REFERENCES work_order_items(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_fg_company ON finished_goods_stocks(company_id);
CREATE INDEX IF NOT EXISTS idx_packing_company ON packing_logs(company_id);
CREATE INDEX IF NOT EXISTS idx_dc_company ON delivery_challans(company_id);
CREATE INDEX IF NOT EXISTS idx_invoice_company ON sales_invoices(company_id);
