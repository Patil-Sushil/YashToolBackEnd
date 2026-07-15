-- ============================================================
-- V10: Quotation Management System - Production Grade
-- ============================================================

-- ============================================================
-- QUOTATIONS TABLE (Root aggregate)
-- ============================================================
CREATE TABLE IF NOT EXISTS quotations (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,

    quotation_no VARCHAR(30) NOT NULL UNIQUE,
    version INTEGER NOT NULL DEFAULT 1,
    parent_quotation_id UUID,
    source_enquiry_id UUID,
    customer_id UUID NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    locked_at TIMESTAMP,
    locked_by VARCHAR(255),

    source_type VARCHAR(20) NOT NULL,
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    valid_until TIMESTAMP NOT NULL,

    customer_company_name VARCHAR(255),
    customer_contact_person VARCHAR(255),
    customer_email VARCHAR(255),
    customer_mobile VARCHAR(20),

    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0,
    taxable_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    cgst_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    sgst_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    igst_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_tax NUMERIC(15, 2) NOT NULL DEFAULT 0,
    grand_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    remarks TEXT,
    internal_notes TEXT,
    terms_and_conditions TEXT,
    payment_terms TEXT,
    delivery_terms TEXT,

    customer_decision VARCHAR(20),
    customer_decision_at TIMESTAMP,
    customer_decision_remarks TEXT,

    revision_required BOOLEAN NOT NULL DEFAULT FALSE,
    revision_count INTEGER NOT NULL DEFAULT 0,
    last_revision_at TIMESTAMP,

    sent_to_customer_at TIMESTAMP,
    sent_to_customer_by VARCHAR(255),

    CONSTRAINT fk_quotation_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_quotation_enquiry FOREIGN KEY (source_enquiry_id) REFERENCES enquiries(id) ON DELETE SET NULL,
    CONSTRAINT fk_quotation_parent FOREIGN KEY (parent_quotation_id) REFERENCES quotations(id) ON DELETE SET NULL,
    CONSTRAINT fk_quotation_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,

    CONSTRAINT chk_quotation_status CHECK (status IN (
                                           'DRAFT','PRICING_READY','PENDING_APPROVAL','APPROVED','REJECTED',
                                           'SENT_TO_CUSTOMER','CUSTOMER_NEGOTIATION','CUSTOMER_APPROVED',
                                           'CUSTOMER_REJECTED','LOCKED','EXPIRED','CANCELLED'
                                                     )),
    CONSTRAINT chk_source_type CHECK (source_type IN ('FROM_ENQUIRY','DIRECT')),
    CONSTRAINT chk_customer_decision CHECK (customer_decision IS NULL
                                            OR customer_decision IN ('APPROVED','REJECTED','NEGOTIATION'))
    );

-- ============================================================
-- QUOTATION ITEMS
-- ============================================================
CREATE TABLE IF NOT EXISTS quotation_items (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),

    quotation_id UUID NOT NULL,
    line_number INTEGER NOT NULL,

    order_type VARCHAR(20) NOT NULL,
    tool_name VARCHAR(200) NOT NULL,
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

    rate_chart_item VARCHAR(255),
    rate_chart_grade VARCHAR(30),
    rate_per_unit NUMERIC(12, 2) NOT NULL,
    standard_rod_length NUMERIC(8, 2) NOT NULL DEFAULT 330.0,
    actual_length_used NUMERIC(8, 2) NOT NULL,
    user_multiplier NUMERIC(5, 2) NOT NULL,

    base_price NUMERIC(12, 2) NOT NULL,
    multiplied_price NUMERIC(12, 2) NOT NULL,
    coating_charge NUMERIC(12, 2) NOT NULL DEFAULT 0,
    unit_price NUMERIC(12, 2) NOT NULL,
    line_subtotal NUMERIC(15, 2) NOT NULL,
    line_discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    line_taxable_amount NUMERIC(15, 2) NOT NULL,
    line_cgst NUMERIC(15, 2) NOT NULL DEFAULT 0,
    line_sgst NUMERIC(15, 2) NOT NULL DEFAULT 0,
    line_igst NUMERIC(15, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(15, 2) NOT NULL,
    hsn_sac_code VARCHAR(10),

    rate_source_table VARCHAR(100),
    rate_record_id VARCHAR(50),
    rate_fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_qitem_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE,
    CONSTRAINT chk_qitem_order_type CHECK (order_type IN ('NEW_TOOL','RESHARPENING','REFORMING')),
    CONSTRAINT chk_qitem_quantity CHECK (quantity > 0),
    CONSTRAINT chk_qitem_rate_per_unit CHECK (rate_per_unit >= 0),
    CONSTRAINT chk_qitem_user_multiplier CHECK (user_multiplier > 0)
    );

-- ============================================================
-- QUOTATION REVISIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS quotation_revisions (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255),

    quotation_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    revision_type VARCHAR(30) NOT NULL,
    revision_reason TEXT NOT NULL,

    previous_subtotal NUMERIC(15, 2),
    previous_discount NUMERIC(15, 2),
    previous_grand_total NUMERIC(15, 2),
    new_subtotal NUMERIC(15, 2),
    new_discount NUMERIC(15, 2),
    new_grand_total NUMERIC(15, 2),

    previous_status VARCHAR(30),
    new_status VARCHAR(30),

    discount_change_amount NUMERIC(15, 2),
    new_discount_percentage NUMERIC(5, 2),

    CONSTRAINT fk_revision_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE,
    CONSTRAINT chk_revision_type CHECK (revision_type IN (
                                        'INITIAL','PRICE_REVISION','DISCOUNT_REVISION','CUSTOMER_FEEDBACK','ADMIN_OVERRIDE','RATE_CHART_UPDATE'
                                                         )),
    UNIQUE(quotation_id, version_number)
    );

-- ============================================================
-- QUOTATION APPROVALS
-- ============================================================
CREATE TABLE IF NOT EXISTS quotation_approvals (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    quotation_id UUID NOT NULL,
    request_id UUID NOT NULL,
    requested_by VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    current_discount_percentage NUMERIC(5, 2) NOT NULL,
    requested_discount_percentage NUMERIC(5, 2) NOT NULL,
    requested_discount_amount NUMERIC(15, 2) NOT NULL,

    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(255),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    approver_comments TEXT,

    approval_threshold NUMERIC(5, 2) NOT NULL,
    exceeds_threshold BOOLEAN NOT NULL,

    CONSTRAINT fk_approval_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE,
    CONSTRAINT chk_approval_status CHECK (approval_status IN ('PENDING','APPROVED','REJECTED','WITHDRAWN'))
    );
    -- V10__inventory_module.sql

    -- 1. Category
    CREATE TABLE IF NOT EXISTS inventory_categories (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(100) NOT NULL,
        code VARCHAR(50) NOT NULL,
        description TEXT,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255),
        CONSTRAINT uk_category_code UNIQUE (code),
        CONSTRAINT uk_category_name UNIQUE (name)
    );

    -- 2. Material Grade
    CREATE TABLE IF NOT EXISTS inventory_material_grades (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(100) NOT NULL,
        code VARCHAR(50) NOT NULL,
        description TEXT,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255),
        CONSTRAINT uk_mat_grade_code UNIQUE (code),
        CONSTRAINT uk_mat_grade_name UNIQUE (name)
    );

    -- 4. Item
    CREATE TABLE IF NOT EXISTS inventory_items (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(150) NOT NULL,
        sku VARCHAR(100) NOT NULL,
        description TEXT,
        category_id UUID NOT NULL REFERENCES inventory_categories(id),
        material_grade_id UUID REFERENCES inventory_material_grades(id),
        active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255),
        CONSTRAINT uk_item_sku UNIQUE (sku)
    );

    -- 5. Stock
    CREATE TABLE IF NOT EXISTS inventory_stocks (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        item_id UUID NOT NULL REFERENCES inventory_items(id),
        material_grade_id UUID REFERENCES inventory_material_grades(id),
        quantity NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255)
    );

    CREATE UNIQUE INDEX uk_stock_composite ON inventory_stocks (item_id) WHERE material_grade_id IS NULL;
    CREATE UNIQUE INDEX uk_stock_composite_with_grade ON inventory_stocks (item_id, material_grade_id) WHERE material_grade_id IS NOT NULL;

    -- 6. Stock Transaction
    CREATE TABLE IF NOT EXISTS inventory_stock_transactions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        transaction_type VARCHAR(50) NOT NULL,
        item_id UUID NOT NULL REFERENCES inventory_items(id),
        material_grade_id UUID REFERENCES inventory_material_grades(id),
        quantity NUMERIC(19, 4) NOT NULL,
        reference_number VARCHAR(100),
        remarks TEXT,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255)
    );

    -- 7. Cut Piece
    CREATE TABLE IF NOT EXISTS inventory_cut_pieces (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        code VARCHAR(50) NOT NULL,
        item_id UUID NOT NULL REFERENCES inventory_items(id),
        material_grade_id UUID NOT NULL REFERENCES inventory_material_grades(id),
        remaining_length NUMERIC(19, 4) NOT NULL,
        status VARCHAR(50) NOT NULL,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255),
        CONSTRAINT uk_cutpiece_code UNIQUE (code)
    );

    -- 8. Material Issue
    CREATE TABLE IF NOT EXISTS inventory_material_issues (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        issue_number VARCHAR(50) NOT NULL,
        item_id UUID NOT NULL REFERENCES inventory_items(id),
        material_grade_id UUID NOT NULL REFERENCES inventory_material_grades(id),
        issue_type VARCHAR(50) NOT NULL,
        cut_piece_id UUID REFERENCES inventory_cut_pieces(id),
        required_length NUMERIC(19, 4) NOT NULL,
        issued_length NUMERIC(19, 4) NOT NULL,
        new_cut_piece_id UUID REFERENCES inventory_cut_pieces(id),
        status VARCHAR(50) NOT NULL,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255),
        CONSTRAINT uk_mat_issue_number UNIQUE (issue_number)
    );

    -- 9. Stock Adjustment
    CREATE TABLE IF NOT EXISTS inventory_stock_adjustments (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        adjustment_number VARCHAR(50) NOT NULL,
        item_id UUID NOT NULL REFERENCES inventory_items(id),
        material_grade_id UUID REFERENCES inventory_material_grades(id),
        quantity NUMERIC(19, 4) NOT NULL,
        adjustment_type VARCHAR(50) NOT NULL,
        reason VARCHAR(50) NOT NULL,
        remarks TEXT,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255),
        CONSTRAINT uk_stock_adj_number UNIQUE (adjustment_number)
    );

    -- 10. Stock Take
    CREATE TABLE IF NOT EXISTS inventory_stock_takes (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        stock_take_number VARCHAR(50) NOT NULL,
        status VARCHAR(50) NOT NULL,
        remarks TEXT,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255),
        CONSTRAINT uk_stock_take_number UNIQUE (stock_take_number)
    );

    -- 11. Stock Take Line
    CREATE TABLE IF NOT EXISTS inventory_stock_take_lines (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        stock_take_id UUID NOT NULL REFERENCES inventory_stock_takes(id) ON DELETE CASCADE,
        item_id UUID NOT NULL REFERENCES inventory_items(id),
        material_grade_id UUID REFERENCES inventory_material_grades(id),
        system_quantity NUMERIC(19, 4) NOT NULL,
        physical_quantity NUMERIC(19, 4) NOT NULL,
        difference_quantity NUMERIC(19, 4) NOT NULL,
        approved BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        created_by VARCHAR(255),
        updated_by VARCHAR(255)
    );


-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_quotation_no ON quotations(quotation_no);
CREATE INDEX IF NOT EXISTS idx_quotation_status ON quotations(status);
CREATE INDEX IF NOT EXISTS idx_quotation_customer ON quotations(customer_id);
CREATE INDEX IF NOT EXISTS idx_quotation_company ON quotations(company_id);
CREATE INDEX IF NOT EXISTS idx_quotation_enquiry ON quotations(source_enquiry_id);
CREATE INDEX IF NOT EXISTS idx_quotation_parent ON quotations(parent_quotation_id);
CREATE INDEX IF NOT EXISTS idx_quotation_created ON quotations(created_at);
CREATE INDEX IF NOT EXISTS idx_quotation_validity ON quotations(valid_until);

CREATE INDEX IF NOT EXISTS idx_qitem_quotation ON quotation_items(quotation_id);
CREATE INDEX IF NOT EXISTS idx_qitem_line ON quotation_items(quotation_id, line_number);
CREATE INDEX IF NOT EXISTS idx_qitem_material_grade ON quotation_items(material_grade);

CREATE INDEX IF NOT EXISTS idx_revision_quotation ON quotation_revisions(quotation_id);
CREATE INDEX IF NOT EXISTS idx_approval_quotation ON quotation_approvals(quotation_id);
CREATE INDEX IF NOT EXISTS idx_approval_status ON quotation_approvals(approval_status);

-- ============================================================
-- TRIGGERS
-- ============================================================
CREATE OR REPLACE FUNCTION update_quotation_lock_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_locked = TRUE AND OLD.is_locked = FALSE THEN
        NEW.locked_at := CURRENT_TIMESTAMP;
    ELSIF NEW.is_locked = FALSE THEN
        NEW.locked_at := NULL;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_quotation_lock
    BEFORE UPDATE ON quotations
    FOR EACH ROW EXECUTE FUNCTION update_quotation_lock_timestamp();

CREATE OR REPLACE FUNCTION update_quotation_customer_decision()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.customer_decision IS NOT NULL
       AND (OLD.customer_decision IS NULL OR OLD.customer_decision <> NEW.customer_decision) THEN
        NEW.customer_decision_at := CURRENT_TIMESTAMP;
        IF NEW.customer_decision = 'APPROVED' THEN NEW.status := 'CUSTOMER_APPROVED';
        ELSIF NEW.customer_decision = 'REJECTED' THEN NEW.status := 'CUSTOMER_REJECTED';
        ELSIF NEW.customer_decision = 'NEGOTIATION' THEN
            NEW.status := 'CUSTOMER_NEGOTIATION';
            NEW.revision_required := TRUE;
END IF;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_quotation_customer_decision
    BEFORE UPDATE ON quotations
    FOR EACH ROW EXECUTE FUNCTION update_quotation_customer_decision();