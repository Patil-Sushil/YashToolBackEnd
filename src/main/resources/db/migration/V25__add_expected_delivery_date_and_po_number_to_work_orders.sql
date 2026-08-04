-- ============================================================
-- V25: Add Expected Delivery Date and PO Number to Work Orders
-- ============================================================

ALTER TABLE work_orders
ADD COLUMN IF NOT EXISTS expected_delivery_date DATE DEFAULT NULL,
ADD COLUMN IF NOT EXISTS po_number VARCHAR(100) DEFAULT NULL;
