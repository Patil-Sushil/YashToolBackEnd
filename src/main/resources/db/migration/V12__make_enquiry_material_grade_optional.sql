-- Drop the view first because it depends on material_grade of new_tool_specs
DROP VIEW IF EXISTS v_enquiry_items_summary;

-- Drop the material_grade column and its check constraint from new_tool_specs
ALTER TABLE new_tool_specs DROP CONSTRAINT IF EXISTS chk_material_grade;
ALTER TABLE new_tool_specs DROP COLUMN IF EXISTS material_grade;

-- Recreate the view without the material_grade column
CREATE OR REPLACE VIEW v_enquiry_items_summary AS
SELECT
    ei.id AS item_id,
    ei.enquiry_id,
    ei.order_type,
    ei.tool_name,
    ei.quantity,
    ei.trial,
    CASE
        WHEN nts.id IS NOT NULL THEN 'NEW_TOOL'
        WHEN rs.id IS NOT NULL THEN 'RESHARPENING'
        WHEN rfs.id IS NOT NULL THEN 'REFORMING'
        ELSE 'UNKNOWN'
        END AS spec_type,
    CASE
        WHEN nts.coating_required = TRUE THEN nts.coating_type
        WHEN rs.coating_required = TRUE THEN rs.coating_type
        WHEN rfs.coating_required = TRUE THEN rfs.coating_type
        ELSE NULL
        END AS coating_type,
    nts.material_type
FROM enquiry_items ei
         LEFT JOIN new_tool_specs nts ON ei.id = nts.enquiry_item_id
         LEFT JOIN resharpening_specs rs ON ei.id = rs.enquiry_item_id
         LEFT JOIN reforming_specs rfs ON ei.id = rfs.enquiry_item_id;

