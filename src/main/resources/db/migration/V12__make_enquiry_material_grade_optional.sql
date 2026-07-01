-- Drop the material_grade column and its check constraint from new_tool_specs
ALTER TABLE new_tool_specs DROP CONSTRAINT IF EXISTS chk_material_grade;
ALTER TABLE new_tool_specs DROP COLUMN IF EXISTS material_grade;
