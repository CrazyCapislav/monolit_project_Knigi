CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

INSERT INTO users (email, display_name, password_hash, role, created_at)
VALUES ('admin@bookswap.com', 
        'Administrator', 
        '$2a$10$XcDaZgXWQ8r5cmMSnW75MOPnooX/W/DUdZ6xbXDzwfev9F35VVqw6',
        'ADMIN',
        NOW());

INSERT INTO users (email, display_name, password_hash, role, created_at)
VALUES ('publisher@bookswap.com',
        'Test Publisher',
        '$2a$10$FoVekwALyJrqMUS/QXdRZuVZRFhJdA1qED9vy8.Pe.sOne0dzrDVG',
        'PUBLISHER',
        NOW());