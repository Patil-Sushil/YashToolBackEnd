-- ============================================================
-- V13: Add item_name column to quotation_items
-- ============================================================

ALTER TABLE quotation_items ADD COLUMN IF NOT EXISTS item_name VARCHAR(200);

-- Populate existing records
UPDATE quotation_items SET item_name = tool_name WHERE item_name IS NULL;

-- Make it NOT NULL
ALTER TABLE quotation_items ALTER COLUMN item_name SET NOT NULL;

-- V13__remove_warehouse.sql

-- Drop warehouse_id from inventory_stocks if exists
ALTER TABLE inventory_stocks DROP COLUMN IF EXISTS warehouse_id;

-- Drop warehouse_id and destination_warehouse_id from inventory_stock_transactions if exists
ALTER TABLE inventory_stock_transactions DROP COLUMN IF EXISTS warehouse_id;
ALTER TABLE inventory_stock_transactions DROP COLUMN IF EXISTS destination_warehouse_id;

-- Drop warehouse_id from inventory_cut_pieces if exists
ALTER TABLE inventory_cut_pieces DROP COLUMN IF EXISTS warehouse_id;

-- Drop warehouse_id from inventory_material_issues if exists
ALTER TABLE inventory_material_issues DROP COLUMN IF EXISTS warehouse_id;

-- Drop warehouse_id from inventory_stock_adjustments if exists
ALTER TABLE inventory_stock_adjustments DROP COLUMN IF EXISTS warehouse_id;

-- Drop warehouse_id from inventory_stock_takes if exists
ALTER TABLE inventory_stock_takes DROP COLUMN IF EXISTS warehouse_id;

-- Drop table inventory_warehouses if exists
DROP TABLE IF EXISTS inventory_warehouses CASCADE;
