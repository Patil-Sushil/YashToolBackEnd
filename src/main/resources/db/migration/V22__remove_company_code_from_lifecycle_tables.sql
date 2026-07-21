-- ============================================================
-- V22: Remove Redundant company_code Column from Lifecycle Tables
-- ============================================================

ALTER TABLE finished_goods_stocks DROP COLUMN IF EXISTS company_code;
ALTER TABLE packing_logs DROP COLUMN IF EXISTS company_code;
ALTER TABLE delivery_challans DROP COLUMN IF EXISTS company_code;
ALTER TABLE sales_invoices DROP COLUMN IF EXISTS company_code;
