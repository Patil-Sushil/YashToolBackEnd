-- V27: Remove Redundant company_code Column from Transit Damage Reports
-- Align with V20 and V22 database schema design standards.

ALTER TABLE transit_damage_reports DROP COLUMN IF EXISTS company_code;
