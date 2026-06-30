CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    trust_score DECIMAL(5, 2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);
