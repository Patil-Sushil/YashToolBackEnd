-- ============================================================
-- V24: Add Trial Status and Feedback to Work Order Items
-- ============================================================

ALTER TABLE work_order_items 
ADD COLUMN IF NOT EXISTS trial_status VARCHAR(30) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS trial_feedback TEXT;

-- Enforce CHECK constraint on trial_status values
ALTER TABLE work_order_items
ADD CONSTRAINT chk_woi_trial_status CHECK (trial_status IN ('PENDING', 'SUCCESS', 'FAILED') OR trial_status IS NULL);
