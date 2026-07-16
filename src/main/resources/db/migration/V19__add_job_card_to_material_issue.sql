-- ============================================================
-- V19: Add Job Card Link to Material Issues Table
-- ============================================================

ALTER TABLE inventory_material_issues
ADD COLUMN job_card_id UUID;

ALTER TABLE inventory_material_issues
ADD CONSTRAINT fk_material_issue_job_card
FOREIGN KEY (job_card_id)
REFERENCES job_cards(id)
ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_material_issue_job_card ON inventory_material_issues(job_card_id);
