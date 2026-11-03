ALTER TABLE holdings
ADD COLUMN category VARCHAR(50) DEFAULT 'Aktier';

-- Demo data: treat a few seeded holdings as funds so accounts have both categories
UPDATE holdings SET category = 'Fonder' WHERE ticker IN ('VOLV-B', 'SWED-A');