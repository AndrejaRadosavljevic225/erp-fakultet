-- Kreira po jednu bazu za svaki mikroservis.
-- POSTGRES_DB=erp_hr vec kreira prvu bazu; ovde pravimo ostale.
-- Izvrsava se samo pri PRVOM podizanju volumena (prazan postgres-data).

CREATE DATABASE erp_finance;
CREATE DATABASE erp_schedule;

-- Sve baze su u vlasnistvu default korisnika 'erp' (POSTGRES_USER).
