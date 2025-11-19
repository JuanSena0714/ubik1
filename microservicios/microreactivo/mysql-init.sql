CREATE DATABASE IF NOT EXISTS customers_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS products_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS orders_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'demo'@'localhost' IDENTIFIED BY 'demo';
GRANT ALL PRIVILEGES ON customers_db.* TO 'demo'@'localhost';
GRANT ALL PRIVILEGES ON products_db.*  TO 'demo'@'localhost';
GRANT ALL PRIVILEGES ON orders_db.*    TO 'demo'@'localhost';
FLUSH PRIVILEGES;
