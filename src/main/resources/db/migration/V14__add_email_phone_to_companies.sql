-- ============================================================
-- V14: Add email and phone columns to companies table
-- Target DB: PostgreSQL
-- ============================================================

ALTER TABLE companies ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE companies ADD COLUMN IF NOT EXISTS phone VARCHAR(50);

-- Update seeded companies with initial email and phone details
UPDATE companies SET email = 'SWARA.ENTERPRISES5555@GMAIL.COM', phone = '7709228568 / 8149818555' WHERE code = 'SW';
UPDATE companies SET email = 'info@yashtools.com', phone = '02712-234567' WHERE code = 'YT';
