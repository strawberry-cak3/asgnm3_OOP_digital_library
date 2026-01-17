# asgnm3_OOP_digital_library

CREATE TABLE books (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publication_year SMALLINT NOT NULL,
    genre VARCHAR(100),
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE users (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    registration_date DATE NOT NULL,
    user_type VARCHAR(10) NOT NULL CHECK (user_type IN ('regular', 'premium'))
);
