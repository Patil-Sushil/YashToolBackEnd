-- ============================================================
-- V26: Remove Actual Start and End Dates from Work Orders
-- ============================================================

ALTER TABLE work_orders
DROP COLUMN IF EXISTS actual_start_date,
DROP COLUMN IF EXISTS actual_end_date;
