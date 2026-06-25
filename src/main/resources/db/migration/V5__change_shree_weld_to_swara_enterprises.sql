-- Update company name from 'Shree Weld' to 'Swara Enterprises' for existing databases
UPDATE companies 
SET name = 'Swara Enterprises' 
WHERE code = 'SW' AND name = 'Shree Weld';
