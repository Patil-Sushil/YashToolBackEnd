-- ============================================================
-- V20: Remove Redundant company_code Column from Tables
-- ============================================================

-- Drop company_code column from tables where company_id is present
-- and company_code is redundant and not mapped in Java entities.
ALTER TABLE quotations DROP COLUMN IF EXISTS company_code;
ALTER TABLE work_orders DROP COLUMN IF EXISTS company_code;
ALTER TABLE machines DROP COLUMN IF EXISTS company_code;
ALTER TABLE job_cards DROP COLUMN IF EXISTS company_code;
ALTER TABLE production_schedules DROP COLUMN IF EXISTS company_code;
ALTER TABLE quality_inspections DROP COLUMN IF EXISTS company_code;
