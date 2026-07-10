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

