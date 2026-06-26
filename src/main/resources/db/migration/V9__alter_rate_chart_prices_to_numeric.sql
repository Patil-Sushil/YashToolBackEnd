-- Alter price columns in rate charts to NUMERIC(38, 2) to match JPA BigDecimal mapping
ALTER TABLE hyperion_coolant_hole_rod_price ALTER COLUMN price TYPE NUMERIC(38, 2);

ALTER TABLE hyperion_rod_net_price ALTER COLUMN k40uf_h10f TYPE NUMERIC(38, 2);
ALTER TABLE hyperion_rod_net_price ALTER COLUMN am70_dm80 TYPE NUMERIC(38, 2);
ALTER TABLE hyperion_rod_net_price ALTER COLUMN pn90 TYPE NUMERIC(38, 2);
ALTER TABLE hyperion_rod_net_price ALTER COLUMN gp10_k10f TYPE NUMERIC(38, 2);
