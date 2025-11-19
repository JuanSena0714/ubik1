CREATE DATABASE IF NOT EXISTS customers_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS products_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS orders_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS booking_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'demo'@'localhost' IDENTIFIED BY 'demo';
GRANT ALL PRIVILEGES ON customers_db.* TO 'demo'@'localhost';
GRANT ALL PRIVILEGES ON products_db.*  TO 'demo'@'localhost';
GRANT ALL PRIVILEGES ON orders_db.*    TO 'demo'@'localhost';
GRANT ALL PRIVILEGES ON booking_db.*   TO 'demo'@'localhost';
GRANT ALL PRIVILEGES ON booking_db.*   TO 'root'@'localhost';
FLUSH PRIVILEGES;

-- Tabla de reservas (Booking Service)
USE booking_db;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    motel_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    guest_name VARCHAR(100) NOT NULL,
    guest_email VARCHAR(100) NOT NULL,
    guest_phone VARCHAR(20) NOT NULL,
    special_requests TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id),
    INDEX idx_motel_id (motel_id),
    INDEX idx_status (status),
    INDEX idx_check_in_date (check_in_date),
    INDEX idx_check_out_date (check_out_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
