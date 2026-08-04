-- ============================================================
-- V23: Transit Damage Management
-- ============================================================

CREATE TABLE IF NOT EXISTS transit_damage_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    company_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    delivery_challan_id UUID NOT NULL,
    work_order_item_id UUID NOT NULL,
    damaged_quantity INTEGER NOT NULL CHECK (damaged_quantity > 0),
    action VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',
    reported_by VARCHAR(255),
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    remarks TEXT,
    created_job_card_id UUID,

    CONSTRAINT fk_tdr_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tdr_challan FOREIGN KEY (delivery_challan_id) REFERENCES delivery_challans(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tdr_wo_item FOREIGN KEY (work_order_item_id) REFERENCES work_order_items(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tdr_job_card FOREIGN KEY (created_job_card_id) REFERENCES job_cards(id) ON DELETE SET NULL,
    CONSTRAINT chk_tdr_action CHECK (action IN ('REWORK', 'REPLACE', 'CREDIT_ONLY')),
    CONSTRAINT chk_tdr_status CHECK (status IN ('REPORTED', 'APPROVED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_tdr_company ON transit_damage_reports(company_id);
CREATE INDEX IF NOT EXISTS idx_tdr_challan ON transit_damage_reports(delivery_challan_id);
CREATE INDEX IF NOT EXISTS idx_tdr_wo_item ON transit_damage_reports(work_order_item_id);
