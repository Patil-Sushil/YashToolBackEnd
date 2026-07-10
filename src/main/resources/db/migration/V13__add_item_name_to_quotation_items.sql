-- ============================================================
-- V13: Add item_name column to quotation_items
-- ============================================================

ALTER TABLE quotation_items ADD COLUMN item_name VARCHAR(200);

-- Populate existing records
UPDATE quotation_items SET item_name = tool_name WHERE item_name IS NULL;

-- Make it NOT NULL
ALTER TABLE quotation_items ALTER COLUMN item_name SET NOT NULL;
