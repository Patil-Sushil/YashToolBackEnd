-- ============================================================
-- V3: Multi-Company (Multi-Tenancy) Support
-- Target DB: PostgreSQL
-- ============================================================

-- Alter tables to add company_code
ALTER TABLE users ADD COLUMN company_code VARCHAR(50);
ALTER TABLE customers ADD COLUMN company_code VARCHAR(50);
ALTER TABLE enquiries ADD COLUMN company_code VARCHAR(50);
ALTER TABLE enquiry_items ADD COLUMN company_code VARCHAR(50);
ALTER TABLE new_tool_specs ADD COLUMN company_code VARCHAR(50);
ALTER TABLE reforming_specs ADD COLUMN company_code VARCHAR(50);
ALTER TABLE resharpening_specs ADD COLUMN company_code VARCHAR(50);
ALTER TABLE laborers ADD COLUMN company_code VARCHAR(50);
ALTER TABLE attendance ADD COLUMN company_code VARCHAR(50);
ALTER TABLE advance_transactions ADD COLUMN company_code VARCHAR(50);
ALTER TABLE weekly_payouts ADD COLUMN company_code VARCHAR(50);

-- Set default company_code for existing data
UPDATE users SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE customers SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE enquiries SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE enquiry_items SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE new_tool_specs SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE reforming_specs SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE resharpening_specs SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE laborers SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE attendance SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE advance_transactions SET company_code = 'YT' WHERE company_code IS NULL;
UPDATE weekly_payouts SET company_code = 'YT' WHERE company_code IS NULL;

-- Alter columns to be NOT NULL
ALTER TABLE users ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE customers ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE enquiries ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE enquiry_items ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE new_tool_specs ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE reforming_specs ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE resharpening_specs ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE laborers ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE attendance ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE advance_transactions ALTER COLUMN company_code SET NOT NULL;
ALTER TABLE weekly_payouts ALTER COLUMN company_code SET NOT NULL;

-- Modify unique constraints
ALTER TABLE enquiries DROP CONSTRAINT IF EXISTS enquiries_enquiry_no_key;
ALTER TABLE enquiries ADD CONSTRAINT uq_enquiry_company_no UNIQUE (company_code, enquiry_no);

ALTER TABLE customers DROP CONSTRAINT IF EXISTS customers_mobile_number_key;
ALTER TABLE customers DROP CONSTRAINT IF EXISTS customers_email_key;
ALTER TABLE customers ADD CONSTRAINT uq_customer_company_mobile UNIQUE (company_code, mobile_number);
ALTER TABLE customers ADD CONSTRAINT uq_customer_company_email UNIQUE (company_code, email);
