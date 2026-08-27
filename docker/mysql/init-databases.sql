-- Baze za mikroservise (MySQL varijanta). Svaki servis ima svoju bazu,
-- isto kao u Postgres postavci.
CREATE DATABASE IF NOT EXISTS erp_hr CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_schedule CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_finance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON erp_hr.* TO 'erp'@'%';
GRANT ALL PRIVILEGES ON erp_schedule.* TO 'erp'@'%';
GRANT ALL PRIVILEGES ON erp_finance.* TO 'erp'@'%';
FLUSH PRIVILEGES;
