-- ============================================================
-- V4: Authentication, Authorization & Multi-Company Migration
-- Target DB: PostgreSQL
-- ============================================================

-- Create companies table
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    gst_number VARCHAR(20),
    bank_name VARCHAR(255),
    bank_account_no VARCHAR(50),
    bank_ifsc VARCHAR(20),
    bank_branch VARCHAR(255),
    address TEXT,
    logo_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Seed initial companies (YT - Yash Tools, SW - Swara Enterprises)
INSERT INTO companies (id, code, name, gst_number, bank_name, bank_account_no, bank_ifsc, bank_branch, address)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'YT', 'Yash Tools', '27AAAAY0000A1Z1', 'State Bank of India', '12345678901', 'SBIN0000001', 'Main Branch', 'Industrial Area, Maharashtra'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'SW', 'Swara Enterprises', '27AAAAS9999B1Z2', 'HDFC Bank', '98765432109', 'HDFC0000123', 'City Branch', 'MIDC, Maharashtra')
ON CONFLICT (code) DO NOTHING;

-- Add company_id UUID column to customers and enquiries
ALTER TABLE customers ADD COLUMN company_id UUID REFERENCES companies(id);
ALTER TABLE enquiries ADD COLUMN company_id UUID REFERENCES companies(id);

-- Map existing records based on company_code
UPDATE customers SET company_id = (SELECT id FROM companies WHERE code = customers.company_code);
UPDATE enquiries SET company_id = (SELECT id FROM companies WHERE code = enquiries.company_code);

-- Fallback defaults if null
UPDATE customers SET company_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' WHERE company_id IS NULL;
UPDATE enquiries SET company_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' WHERE company_id IS NULL;

-- Alter columns to be NOT NULL
ALTER TABLE customers ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE enquiries ALTER COLUMN company_id SET NOT NULL;

-- Modify unique constraints to use company_id
ALTER TABLE enquiries DROP CONSTRAINT IF EXISTS uq_enquiry_company_no;
ALTER TABLE enquiries ADD CONSTRAINT uq_enquiry_company_no UNIQUE (company_id, enquiry_no);

ALTER TABLE customers DROP CONSTRAINT IF EXISTS uq_customer_company_mobile;
ALTER TABLE customers DROP CONSTRAINT IF EXISTS uq_customer_company_email;
ALTER TABLE customers ADD CONSTRAINT uq_customer_company_mobile UNIQUE (company_id, mobile_number);
ALTER TABLE customers ADD CONSTRAINT uq_customer_company_email UNIQUE (company_id, email);

-- Drop company_code from Layer 1 & Child & Layer 2 tables where it is not needed
ALTER TABLE customers DROP COLUMN company_code;
ALTER TABLE enquiries DROP COLUMN company_code;
ALTER TABLE enquiry_items DROP COLUMN company_code;
ALTER TABLE new_tool_specs DROP COLUMN company_code;
ALTER TABLE reforming_specs DROP COLUMN company_code;
ALTER TABLE resharpening_specs DROP COLUMN company_code;
ALTER TABLE laborers DROP COLUMN company_code;
ALTER TABLE attendance DROP COLUMN company_code;
ALTER TABLE advance_transactions DROP COLUMN company_code;
ALTER TABLE weekly_payouts DROP COLUMN company_code;
